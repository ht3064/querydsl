package study.querydsl.domain.member.dao;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import study.querydsl.domain.member.dto.request.MemberSearchCondition;
import study.querydsl.domain.member.dto.response.MemberTeamDto;
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

        Long total =
                queryFactory
                        .select(member.count())
                        .from(member)
                        .leftJoin(member.team, team)
                        .where(
                                usernameEq(condition.username()),
                                teamNameEq(condition.teamName()),
                                ageGoe(condition.ageGoe()),
                                ageLoe(condition.ageLoe()))
                        .fetchOne();

        return new PageImpl<>(results, pageable, total);
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