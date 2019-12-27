package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

// 5 번째 생성
// 추상클래스로 작성함 상속을 해줘야하는 클래스이기 때문이다
// 상속 관련 annotation을 정의해줘야 한다 Inheritance, DiscriminatorColumn
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
@Setter
public abstract class Item {

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    // Category와 다대다 관계를 맺고 있다
    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    // MemberServiceTest 완료 이후의 비즈니즈 로직을 추가하는 과정
    // 데이터를 바꿔야 하는 비즈니스 로직이 있는 경우에는 해당하는 엔티티 내에 비즈니스 로직이 존재하는 것이 좋다
    // 재고 수량을 늘리는 로직
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    // 재고 수량을 줄이는 로직
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if(restStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity -= quantity;
    }
}
