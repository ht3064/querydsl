package study.querydsl.domain.member.dao;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.support.PageableExecutionUtils;
import study.querydsl.domain.member.dto.request.MemberSearchCondition;
import study.querydsl.domain.member.dto.response.MemberTeamDto;
import study.querydsl.domain.member.dto.response.MemberTeamDtoV2;
import study.querydsl.domain.member.dto.response.QMemberTeamDto;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.domain.member.domain.QMember.member;
import static study.querydsl.domain.team.domain.QTeam.team;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
                .select(
                        new QMemberTeamDto(
                                member.id,
                                member.username,
                                member.age,
                                team.id,
                                team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.username()),
                        teamNameEq(condition.teamName()),
                        ageGoe(condition.ageGoe()),
                        ageLoe(condition.ageLoe()))
                .fetch();
    }

    @Override
    public Page<MemberTeamDto> searchPage(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> results =
                queryFactory
                        .select(
                                new QMemberTeamDto(
                                        member.id,
                                        member.username,
                                        member.age,
                                        team.id,
                                        team.name))
                        .from(member)
                        .leftJoin(member.team, team)
                        .where(
                                usernameEq(condition.username()),
                                teamNameEq(condition.teamName()),
                                ageGoe(condition.ageGoe()),
                                ageLoe(condition.ageLoe()))
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        JPAQuery<Long> countQuery =
                queryFactory
                        .select(member.count())
                        .from(member)
                        .leftJoin(member.team, team)
                        .where(
                                usernameEq(condition.username()),
                                teamNameEq(condition.teamName()),
                                ageGoe(condition.ageGoe()),
                                ageLoe(condition.ageLoe()));

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    @Override
    public Slice<MemberTeamDtoV2> searchSlice(
            MemberSearchCondition condition, Long lastMemberId, int pageSize) {
        List<MemberTeamDtoV2> results =
                queryFactory
                        .select(Projections.constructor(
                                MemberTeamDtoV2.class,
                                member.id,
                                member.username,
                                member.age,
                                team.id,
                                team.name))
                        .from(member)
                        .leftJoin(member.team, team)
                        .where(
                                lastMemberId(lastMemberId),
                                usernameEq(condition.username()),
                                teamNameEq(condition.teamName()),
                                ageGoe(condition.ageGoe()),
                                ageLoe(condition.ageLoe()))
                        .orderBy(member.updatedAt.desc())
                        .limit((long) pageSize + 1)
                        .fetch();

        return checkLastPage(results, pageSize);
    }

    private BooleanExpression lastMemberId(Long lastMemberId) {
        if (lastMemberId == null) {
            return null;
        }
        return member.id.lt(lastMemberId);
    }

    private Slice<MemberTeamDtoV2> checkLastPage(List<MemberTeamDtoV2> results, int pageSize) {
        boolean hasNext = false;

        if (results.size() > pageSize) {
            hasNext = true;
            results.remove(pageSize);
        }

        PageRequest pageRequest = PageRequest.ofSize(pageSize);

        return new SliceImpl<>(results, pageRequest, hasNext);
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}