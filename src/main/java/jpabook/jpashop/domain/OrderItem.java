package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

// 4 번째 생성
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    // Foreign Key 값을 JoinColunm으로 설정
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; // 주문 가격
    private int count; // 주문 수량






    // 생성 메서드
    // 생성 메서드를 통해서만 생성해라 라는 annotation @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count);
        return orderItem;
    }





    // Order의 비즈니스 로직인 cancel 함수에서 넘어온 함수이다. 여기서 주문 수량을 바꿔줘야 함
    public void cancel() {
        getItem().addStock(count);
    }

    // Order의 조회 로직인 getTotalPrice 함수에서 넘어온 함수이다. 여기서 주문 가격을 리턴해 줘야함
    public int getTotalPrice() {
        return getOrderPrice() *  getCount();
    }
}
