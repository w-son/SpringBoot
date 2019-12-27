package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

// @Controller @ResponseBody
@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /*
     회원을 등록하는 api 이다
     api 통신은 무엇으로 하느냐? --> json 으로 할 것
     valid 은 지금 들어오는 파라미터가 "엔티티 내의" validation을 전부 지키는지 확인하는 용도
     그런데 이런식으로 validation을 엔티티 단에서 확인을 하면 엔티티 수정 시 api 스펙이 바뀌어 버리게 된다
     따라서 api 통신을 할 때에는 엔티티로 받으면 안됨
     엔티티를 외부에 노출(파라미터로 엔티티를 받아줌)해서 한다는 것 자체가 유지보수에 굉장히 좋지 않음
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {

        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }
    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
    // 위에서 언급했던 엔티티의 스펙을 여기서 조작하면 된다. @NotEmpty같은 것들을 여기서 처리해주면 됨
    @Data
    static class CreateMemberRequest {

        @NotEmpty
        private String name;
    }





    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }
    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {

        private Long id;
        private String name;
    }
    @Data
    static class UpdateMemberRequest {

        @NotEmpty
        private String name;
    }





    /*
     여기서 생기는 문제는 무엇이냐
     마찬가지로 엔티티에 있는 정보들이 외부에 다 노출이 된다
     다양한 형태의 api가 요구될텐데 엔티티 자체를 리턴하는 방식으로 코딩하지 말자
     api 스펙 자체가 바뀌어버림
     스펙이 바뀐다는 것... List 형태로 리턴을 하게 되면 json array 형태로 리턴이 된다 [ 여기 안에 엔티티 유형이 들어감 ]
     ex) [ { a : "" }, { a : "" }, { a : "" } ]
     -> 이런식이면 스펙 확장이 어려움 a 형태의 객체만 스펙에 들어갈 수 있다
     */
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }
    @GetMapping("/api/v2/members")
    public Result memberV2() {
        List<Member> findMembers = memberService.findMembers();
        // 그냥 for 문 돌려서 집어 넣는 거랑 비슷함
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect.size(), collect);
    }
    // json array 타입으로 나가지 않게 한번 감싸주는 과정이다
    @Data
    @AllArgsConstructor
    static class Result<T> {
        // count <-- 이런게 추가될 수 있다는 의미이다
        private int count;
        private T data;
    }
    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

}
