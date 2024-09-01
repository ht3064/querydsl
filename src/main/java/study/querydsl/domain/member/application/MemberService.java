package study.querydsl.domain.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.member.dao.MemberJpaRepository;
import study.querydsl.domain.member.dto.request.MemberSearchCondition;
import study.querydsl.domain.member.dto.response.MemberTeamDto;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberJpaRepository memberJpaRepository;

    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.searchByWhereParam(condition);
    }
}
