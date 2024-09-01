package study.querydsl.domain.member.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.domain.member.dto.request.MemberSearchCondition;
import study.querydsl.domain.member.dto.response.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
    Page<MemberTeamDto> searchPage(MemberSearchCondition condition, Pageable pageable);
}