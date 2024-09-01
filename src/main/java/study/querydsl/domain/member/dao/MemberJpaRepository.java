package study.querydsl.domain.member.dao;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.querydsl.domain.member.domain.Member;
import study.querydsl.domain.member.dto.request.MemberSearchCondition;
import study.querydsl.domain.member.dto.response.MemberTeamDto;
import study.querydsl.domain.member.dto.response.QMemberTeamDto;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.domain.member.domain.QMember.member;
import static study.querydsl.domain.team.domain.QTeam.team;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public void save(Member member) {
        entityManager.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findMember = entityManager.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {
        return entityManager
                .createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findAll_Querydsl() {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findByUsername(String username) {
        return entityManager
                .createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<Member> findByUsername_Querydsl(String username) {
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();

    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();

        if (hasText(condition.username())) {
            builder.and(member.username.eq(condition.username()));
        }

        if (hasText(condition.teamName())) {
            builder.and(team.name.eq(condition.teamName()));
        }

        if (condition.ageGoe() != null) {
            builder.and(member.age.goe(condition.ageGoe()));
        }

        if (condition.ageLoe() != null) {
            builder.and(member.age.loe(condition.ageLoe()));
        }

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
                .where(builder)
                .fetch();
    }

    public List<MemberTeamDto> searchByWhereParam(MemberSearchCondition condition) {
        return queryFactory
                .select(
                        new QMemberTeamDto(
                                member.id, member.username,
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