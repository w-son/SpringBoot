package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.OrderSimpleQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/*
 x to one (OneToOne 과 ManyToOne에서 성능 최적화를 어떻게 할 것이냐)
 Order 조회
 Order -> Member
 Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {

        /*
            1) 첫 번째 문제
            밑과 같은 방식을 취하면 리스트 반환이 무한으로 일어난다 ... 왜??
            Order에서 Member로 가야함
            Member로 감 Member에서는 orders를 리턴해줌
            각 orders의 Order에서 Member로 매핑이됨
            --> 이런식으로 계속 무한루프가 돈다
            따라서 양방향 연관관계가 있으면 한쪽에 @JsonIgnore annotation을 넣어줘야 한다
            이 예제의 경우에는 Member의 orders와 OrderItem의 order, 그리고 Delivey의 order에 추가하였음

            2) 두 번째 문제
            일단 Order에서 Member로 갔다
            Member의 fetch가 LAZY로 되어있다 그 뜻은?
            이걸 이해하기 위해서 데이터베이스의 LAZY fetch와 EAGER fetch를 이해해야 한다
            LAZY fetch : Member를 가져오면 Member 그 자체만 가져온다
            EAGER fetch : Member를 가져오면 Member 내에 있던 다른 매핑되는 attribute들 대상으로도 쿼리를 보내서 가져오게 된다

            지금 Order의 member가 LAZY 타입으로 fetch 되어있는데 여기서 데이터베이스의 실제 멤버 값을 가져오지 않고
            프록시 타입의 멤버 객체로 초기화하게 된다
            --> 이 과정을 거치지 않도록 build.gradle 에다가 추가한 후 main 함수에다 등록하면 된다
            --> 그렇게 되면 기본 설정을 프록시가 아닌 사용자 설정에 의한 값으로 세팅을 할 수 있다
        */
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        // 원하는 정보만 끌어다가 리턴하고 싶은 경우
        for (Order order : all) {
            /*
             LAZY loading
             매핑정보를 사용하지 않을 경우는 프록시 객체로 초기화가 되지만 해당 프록시 객체에서 get 함수를 호출하면
             관련된 쿼리문을 호출해 리턴해서 실제 디비에서 정보를 가져오는, 즉 LAZY를 강제로 초기화 하는 효과가 된다
            */
            order.getMember().getName();
            order.getDelivery().getAddress();
        }
        return all;
    }

    /*
     V1과 쿼리수는 1+N으로 같다
     다만 LAZY방식은 영속성 컨텍스트 내에서 이미 조회된 쿼리라면 생략 가능하기 때문에 최대 1+N번의 쿼리가 이뤄진다
    */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    // jpa의 fetch 명령어를 사용하여 sql쿼리 수를 줄이는 방법
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(); // OrderRepository 에 있음
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> orderV4() {
        return orderRepository.findOrderDtos(); // OrderRepository 에 있음

    }

    /*
        정리
        v2 -> v3 : fetch join을 활용해서 쿼리문의 수를 줄였다
        v3 -> v4 : 새로운 Dto와 그게 맞는 쿼리문을 활용해서 fetch join을 통해서 한꺼번에 데이터를 모조리 갖고 오기보단
                   원하는 데이터만 끌고 올 수 있게끔 설계하였다

        그런데 사실상 두 버전의 성능 차이는 크게 없고
        api최적화에 조금 더 적합한 v3가 유용하게 쓰일 수 있다
    */

    // v2, v3
    @Data
    static class SimpleOrderDto {
        /*
         바로 여기서 api 스펙을 명확하게 규정해야 한다
         이런 api 스펙을 서버와 클라이언트 간에 규정을 하면서
         양측에서 엔티티의 어트리뷰트를 갱신하려고 할 때 컴파일 단에서 오류를 발생시킬 수 있도록 프로그래밍 하는 것이다
        */
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            /*
             여기서 생기는 문제점은 무엇이냐? order가 처음에 조회된 이휴에
             member, delivery 세 테이블을 조회해야한다

             정리
             처음에 findAllByString
             ORDER -> SQL 1 -> 2개의 객체가 리턴됨
             루프가 돌 때 2번 돈다
             각각의 루프에 member, delivery 에 대한 SQL 2번씩
             총 5번 쿼리가 나가게 된 것임 -> 굉장히 비효율적이라고 한다
             중요) 이걸 N + 1 문제라고 함
             한번의 쿼리(Order)에 대해 최대 N번(member, delivery)의 쿼리가 추가로 나갈 수 있기 때문
             왜 최대 N번인가? 영속성 context 자체에 쿼리를 보내는 것이기 때문에 그 내에 처음 질의했던 id가 존재하면 다시 물어보지 않음
             ex) for 문 첫번째 userA에 대한 질의를 통해서 정보를 불러들임 두번째 for문에 똑같이 userA에 대한 정보를 물어봄 -> 질의 할 필요가 없다
                 결과적으로 4번(userA에 대한 질의 1번만) 쿼리문이 날라가게 된다

             이런 경우를 해결하기 위해서 v3 을 다뤄볼 것이다
             */
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 조기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }

    }

}
