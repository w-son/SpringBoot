package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 이런 url이 들어왔을 시에
    @GetMapping("/members/new")
    public String createForm(Model model) {
        // 컨트롤러에서 뷰로 넘어갈 때 이 데이터를 실어서 넘기게 된다
        model.addAttribute("memberForm", new MemberForm());
        // 리턴되는 주소로 이동하게 된다
        return "members/createMemberForm";
    }

    // 이런 url에서 떠났을 시에?
    // @Valid memberForm 가 빈 값이 들어가지 않도록 설정해 주는 annotation이다
    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {

        if(result.hasErrors()) {
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/";
    }

    // 사실 이런식으로 엔티티를 직접 가져다 쓰면 좋지 않다 위의 createForm처럼 클래스를 하나 새로 생성해서 다루는 것이 좋음
    @GetMapping("/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/memberList";
    }

}
