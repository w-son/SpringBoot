package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

// 리퍼지토리 1 번째
@Repository
@RequiredArgsConstructor
public class MemberRepository {

    // jpa 이용하기 위해서 추가해야 되는 annotation
    // @PersistenceContext
    // RequiredArgsConstructor 왜 넣었는지 MemberService 클래스 통해서 확인
    private final EntityManager em;


    // 멤버를 저장하는 로직
    // persist 하면 영속성 context에 member를 넣는다 나중에 transaction이 commit되는 시점에 DB에 반영이 된다
    public void save(Member member) {
        em.persist(member);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        // 첫번째에 jpql을 쓰고 두번째에 리턴 타입을 넣으면 된다
        // jpql은 sql과 다르게 테이블에 대해서 질의를 하는 것이 아니라 엔티티에 대해서 질의를 하게 됨
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
