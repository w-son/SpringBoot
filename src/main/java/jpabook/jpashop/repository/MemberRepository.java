package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    /*
     각 Repository들을 보면 save, findOne, findAll과 같이 같은 기능을 하는 메서드를 각각 가지고 있다 -> 코드의 중복
     이 문제를 해결하기 위해서 JpaRepository가 존재한다
     Member는 Type, PK 타입은 Long이다

     기존의 save, findOne, findAll 과 같은 메서드들은 JpaRepository에서 모두 상속 받은 상태이고
     findByName과 같은 메서드들만 여기서 재정의 해주면 된다
    */

    /*
     놀랍게도 아래 한줄로 findByName의 재정의가 끝난다
     jpa에서 알아서 함수 이름을 보고 그에 맞는 sql을 짜줌
     select m from Member m where m.name = :name
    */
    List<Member> findByName(String name);
}
