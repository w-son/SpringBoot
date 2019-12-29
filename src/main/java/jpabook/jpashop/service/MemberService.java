package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    // Autowired 하면 spring 이 spring bean에 있는 MemberRepository를 인젝션 해준다
    // @Autowired
    // Autowired는 생성자 이용하지 않아서 유연하지 않다
    // 1) 직접 생성자를 만들어주거나 2) @RequiredArgsConstructor annotation을 추가해서 자동으로 생성자를 추가하게끔 만들어준다
    private final MemberRepository memberRepository;

    // 회원 가입
    // 여기서 transactional 설정을 달리한다 read 말고 write도 쓸 것이기 때문
    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member); // 중복 회원을 검증하는 로직임
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        // 중복 회원이 있을 시의 예외를 여기서 처리해 줄 것이다
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다");
        }
    }

    // 회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    /* JpaRepository interface가 추가되기 전 findOne
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }
    */
    public Member findOne(Long memberId) {
        return memberRepository.findById(memberId).get();
    }

    /* JpaRepository interface가 추가되기 전 update
    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setName(name);
    }
    */

    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findById(id).get();
        member.setName(name);
    }

}
