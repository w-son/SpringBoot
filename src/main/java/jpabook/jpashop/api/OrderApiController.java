package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/*
 앞서 OrderSimpleApiController 에서 1대1이나 다대1 관계에서의 api를 개발했으면
 이번 api는 일대다 관계, 즉 하나의 쿼리에 row가 몇배로 뻥튀기 될 수 있는 관계(컬렉션이라고도 부른다)에 대해
 어떻게 api를 최적화 시킬 것인지에 대해 설계할 것이다
*/
@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /*
     마찬가지로 엔티티를 직접 노출해버리는 방식이기 때문에
     api스펙이나 유지보수면에서 상당히 좋지 못하다
     OrderSimpleApiController의 v1 참고
    */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {

        List<Order> all =  orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            /*
             LAZY타입의 데이터의 프록시들을 실제 값으로 채워주는 과정 프록시 객체에서 get 함수를 호출하면 강제로 값을 초기화 시킬 수 있음
             FETCH로 설정되어있을 경우 n + 1 문제를 발생시킬수가 있음
            */
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    /*
     V1 -> V2
     V1과 쿼리수는 1+N으로 같다
     다만 LAZY방식은 영속성 컨텍스트 내에서 이미 조회된 쿼리라면 생략 가능하기 때문에 최대 1+N번의 쿼리가 이뤄진다

     Dto 설계 목표
     1) 일대다 관계가 추가 될 시에 엔티티 외부 노출을 막기 위해
     2) 컬렉션의 원하는 데이터만 끌고 오기 위해
    */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {

        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        // 가져온 order 엔티티들의 리스트를 Dto 형태로 변환 시켜주는 과정
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return collect;
    }

    /*
     V2 -> V3
     기존의 LAZY방식의 join에서 쿼리문이 여러개가 나가는 것을 방지하기 위해
     join fetch를 활용하여 재설계
     OrderSimpleApiController의 V3와의 차이점 : 일대다 관게인 Item이 존재함 이를 위해서 Dto를 이중으로 설계했음

     fetch join의 문제점
     1) 쿼리는 한번에 다 보낼 수 있지만 중복되는 데이터를 디비에서 모두 어플리케이션으로 전달하게 된다
    */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {

        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return collect;
    }

    /*
     V3 -> V3-page : 중복되는 데이터 없이 정규화된 상태로 데이터를 발을 수 있다
     (일대다 관계 데이터에서 default_batch_fetch_size 옵션을 추가하여 한번에 size만큼 가져올 수 있도록 설계)
     데이터 베이스를 테이블 a,b,c 가 있다고 하자
     테이블 째로 하나씩 갖고오는것과 테이블 3개를 모두 cross product 시킨 후에 가져오는
     데이터의 중복 여부는 차이가 클 것이다

     인자로 들어간 @RequestParam이 페이징 관련 인자임
     uri 형태 : /api/v3-page/orders?offset=1&limit=100
    */
    @GetMapping("/api/v3-page/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {

        // 먼저 order에서 to one 관계에 있는 member와 delivery는 fetch join으로 가져온다
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return collect;
    }

    /*
     repository.order.query 밑의 3가지 클래스
     OrderQueryRepository, OrderQueryDto, OrderItemQueryDto 확인
    */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }





    /* Dto 클래스 */
    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        /*
         아래와 같이 코딩하게 된다면 Dto가 여전히 엔티티에 의존하게 되는 결과가 된다티 = 엔티티가 외부에 노출됨
         Dto는 엔티티에 대한 의존이 완벽히 없어야 한다
         따라서 OrderItem에 대한 Dto도 생성해줘야함
        */
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {

            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            /*
             order.getOrderItems().stream().forEach(o -> o.getItem().getName());
             orderItems = order.getOrderItems();
            */
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    @Data
    static class OrderItemDto {
        // 원하는 정보들만 담는다
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

}
