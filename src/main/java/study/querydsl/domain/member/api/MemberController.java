package study.querydsl.domain.member.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.domain.member.application.MemberService;
import study.querydsl.domain.member.dto.request.MemberSearchCondition;
import study.querydsl.domain.member.dto.response.MemberTeamDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> memberSearchV1(MemberSearchCondition condition) {
        return memberService.searchMemberV1(condition);
    }
}