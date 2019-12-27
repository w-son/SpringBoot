package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter @Setter
public class MemberForm {

    // 화면에서 왔다갔다하는 엔티티 정보는 form 클래스를 활용하자
    @NotEmpty(message = "회원 이름은 필수입니다")
    private String name;

    private String city;

    private String street;

    private String zipcode;

}
