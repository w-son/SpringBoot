package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

// 3 번째 생성
// jpa의 내장 타입임을 명시해주기 위해서 Embeddable annotation을 설정해준다
// Embeddable한 타입을 쓰는 클래스에는 Embedded annotation을 추가해줘야함 예제의 경우 Member의 address
// 추가적으로 내장 타입 같은 경우에는 Setter를 이용하지 않음
@Embeddable
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    // Embedded 타입과 같은 경우에는 생성자를 protected 또는 public으로 설정해 주어야 함
    // 이런식으로 생성자를 열어두는 이유 : JPA 프록시나 리플렉션 같은 라이브러리를 이용할 떄 필요하다
    protected Address() {}

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
