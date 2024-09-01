package study.querydsl.domain.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.member.dao.MemberJpaRepository;
import study.querydsl.domain.member.dao.MemberRepository;
import study.querydsl.domain.member.dto.request.MemberSearchCondition;
import study.querydsl.domain.member.dto.response.MemberTeamDto;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class MemberService {

    @Deprecated
    private final MemberJpaRepository memberJpaRepository;

    private final MemberRepository memberRepository;

    @Deprecated
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.searchByWhereParam(condition);
    }

    public List<MemberTeamDto> searchMemberV2(MemberSearchCondition condition) {
        return memberRepository.search(condition);
    }

    public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPage(condition, pageable);
    }
}