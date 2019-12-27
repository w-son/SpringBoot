package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    // rollback 되기 전에 존재했던 transaction을 보여주기 위함
    @Autowired EntityManager em;

    @Test
    // @Rollback(false)
    public void 회원가입() throws Exception {
        // given 이런게 주어졌고
        Member member = new Member();
        member.setName("son");

        // when 이걸 실행했으면
        Long saveID = memberService.join(member);

        // then 결과가 이렇게 나와야 해
        em.flush();
        assertEquals(member, memberRepository.findOne(saveID));
    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {
        // given
        Member member1 = new Member();
        member1.setName("son");
        Member member2 = new Member();
        member2.setName("son");

        // when
        memberService.join(member1);
        memberService.join(member2); // 여기서 예외가 발생해야 된다 발생하고 함수 밖으로 나가야 된다

        // then
        fail("예외가 발생해야 한다");
    }
}