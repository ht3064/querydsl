package study.querydsl.domain.member.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Deprecated
    @GetMapping("/v1/members")
    public List<MemberTeamDto> memberSearchV1(MemberSearchCondition condition) {
        return memberService.searchMemberV1(condition);
    }

    @GetMapping("/v2/members")
    public List<MemberTeamDto> memberSearchV2(MemberSearchCondition condition) {
        return memberService.searchMemberV2(condition);
    }

    @GetMapping("/v3/members")
    public Page<MemberTeamDto> memberSearchV3(MemberSearchCondition condition, Pageable pageable) {
        return memberService.searchMemberV3(condition, pageable);
    }
}