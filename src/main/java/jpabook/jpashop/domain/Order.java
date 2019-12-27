package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 2 번째 생성
// 1) Table annotation으로 테이블의 이름을 바꿀 수도 있다
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    // 엔티티 설계시 주의점 : ManyToOne 은 기본이 EAGER, 이렇게 되면 Query에 대한 모든 인스턴스를 가져온다
    //                   OneToMany 는 기본이 LAZY 해당하는 인스턴스 하나를 가져온다

    // 2) 하나의 멤버가 여러개의 주문을 가질 수 있으므로 ManyToOne annotation을 추가해준다 -- Member의 orderlist 확인
    // 3) 추가적으로 Member의 foreign_key을 JoinColumn으로 명시해준다
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 모든 엔티티는 저장을 하고 싶으면 엔트리 하나당 각각 persist를 해주어야되는데 cascade가 설정되어 있는 경우에는 이 과정을 생략해도 된다
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate; // 주문시간

    // enum을 쓸 때 반드시 들어가야 하는 annotation
    @Enumerated(EnumType.STRING)
    private OrderStatus status; // 주문상태 [ORDER, CANCEL]





    // 연관관계 메서드
    // 연관관계 메서드의 위치는 중간에 껴 있는 놈을 중심으로 하면 좋다
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }
    /* 원래는 이런식으로 되는 과정
    public static void main(String[] args) {
        Member member = new Member();
        Order order = new Order();
        member.getOrderList().add(order);
        order.setMember(member);
    }
    */
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }





    // 생성 메서드
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for(OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    // 비즈니스 로직
    // 주문 취소
    public void cancel() {
        if(delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("배송완료된 상품은 취소가 불가능합니다");
        }

        this.setStatus(OrderStatus.CANCEL);
        for(OrderItem orderItem : this.orderItems) {
            orderItem.cancel();
        }
    }

    // 조회 로직
    // 전체 가격 조회
    public int getTotalPrice() {
        int totalPrice = 0;
        for(OrderItem orderItem : this.orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }
}





