package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

// 1 번째 생성
@Entity
@Getter
@Setter
public class Member {

    // 1) id 의 데이터베이스 애트리뷰트 이름을 member_id로 설정한다
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    // @NotEmpty
    private String name;

    @Embedded
    private Address address;

    // 2) 하나의 멤버가 여러개의 주문을 가질 수 있으므로 OneToMany annotation을 추가해준다
    // 3) mappedBy Order 테이블에 있는 member에 의해서 mapping이 된거다 그러니 수정은 Order의 member에서만!  읽기전용
    @JsonIgnore
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

}
