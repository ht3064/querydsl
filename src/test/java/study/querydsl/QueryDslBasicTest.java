package study.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.member.domain.Member;
import study.querydsl.domain.member.domain.QMember;
import study.querydsl.domain.member.dto.*;
import study.querydsl.domain.team.domain.Team;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.domain.member.domain.QMember.member;
import static study.querydsl.domain.team.domain.QTeam.team;
import static study.querydsl.domain.team.domain.Team.createTeam;

@SpringBootTest
@Transactional
public class QueryDslBasicTest {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    JPAQueryFactory jpaQueryFactory;

    @BeforeEach
    public void before() {
        Team teamA = createTeam("teamA");
        Team teamB = createTeam("teamB");
        entityManager.persist(teamA);
        entityManager.persist(teamB);

        Member member1 = Member.createMember("member1", 10, teamA);
        Member member2 = Member.createMember("member2", 20, teamA);

        Member member3 = Member.createMember("member3", 30, teamB);
        Member member4 = Member.createMember("member4", 40, teamB);

        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(member3);
        entityManager.persist(member4);
    }

    @Test
    public void JPQL_테스트() {
        // member1을 찾아라.
        Member findMember =
                entityManager
                        .createQuery("select m from Member m where m.username = :username", Member.class)
                        .setParameter("username", "member1")
                        .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void Querydsl_테스트() {
        // member1을 찾아라.
        Member findMember =
                jpaQueryFactory
                        .selectFrom(member)
                        .where(member.username.eq("member1")) // 파라미터 바인딩 처리
                        .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void 검색() {
        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(10);
    }


    @Test
    public void And_파라미터_검색() {
        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(10);
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void 정렬() {
        entityManager.persist(Member.createMember(null, 100, null));
        entityManager.persist(Member.createMember("member5", 100, null));
        entityManager.persist(Member.createMember("member6", 100, null));

        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void 페이징() {
        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void aggregation() {
        List<Tuple> result =
                jpaQueryFactory
                        .select(member.count(),
                                member.age.sum(),
                                member.age.avg(),
                                member.age.max(),
                                member.age.min())
                        .from(member)
                        .fetch();

        Tuple tuple = result.get(0);

        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void groupBy() {
        List<Tuple> result =
                jpaQueryFactory
                        .select(team.name,
                                member.age.avg())
                        .from(member)
                        .join(member.team, team)
                        .groupBy(team.name)
                        .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    public void join() {
        List<Member> result =
                jpaQueryFactory
                        .selectFrom(member)
                        .join(member.team, team)
                        .where(team.name.eq("teamA"))
                        .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() {
        entityManager.persist(Member.createMember("teamA", 0, null));
        entityManager.persist(Member.createMember("teamB", 0, null));

        List<Member> result =
                jpaQueryFactory
                        .select(member)
                        .from(member, team)
                        .where(member.username.eq(team.name))
                        .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /**
     * 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: select m, t from Member m left join m.team t on t.name = 'teamA'
     */
    @Test
    public void join_on_filtering() {
        List<Tuple> result =
                jpaQueryFactory
                        .select(member, team)
                        .from(member)
                        .leftJoin(member.team, team)
                        .on(team.name.eq("teamA"))
                        .fetch();

        result.forEach(tuple -> System.out.println("tuple = " + tuple));
    }

    /**
     * 연관관계 없는 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     */
    @Test
    public void join_on_no_relation() {
        entityManager.persist(Member.createMember("teamA", 0, null));
        entityManager.persist(Member.createMember("teamB", 0, null));

        List<Tuple> result =
                jpaQueryFactory
                        .select(member, team)
                        .from(member)
                        .join(team)
                        .on(member.username.eq(team.name))
                        .fetch();

        result.forEach(tuple -> System.out.println("tuple = " + tuple));
    }

    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;

    @Test
    public void fetchJoinNo() {
        entityManager.flush();
        entityManager.clear();

        Member findMember =
                jpaQueryFactory
                        .selectFrom(member)
                        .where(member.username.eq("member1"))
                        .fetchOne();

        boolean loaded =
                entityManagerFactory.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    @Test
    public void fetchJoinUse() {
        entityManager.flush();
        entityManager.clear();

        Member findMember =
                jpaQueryFactory
                        .selectFrom(member)
                        .join(member.team, team).fetchJoin()
                        .where(member.username.eq("member1"))
                        .fetchOne();

        boolean loaded =
                entityManagerFactory.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        assertThat(loaded).as("페치 조인 적용").isTrue();
    }

    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result =
                jpaQueryFactory
                        .selectFrom(member)
                        .where(member.age.eq(
                                select(memberSub.age.max())
                                        .from(memberSub)))
                        .fetch();

        assertThat(result)
                .extracting("age")
                .containsExactly(40);
    }

    /**
     * 나이가 평군 이상인 회원
     */
    @Test
    public void subQueryGoe() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result =
                jpaQueryFactory
                        .selectFrom(member)
                        .where(member.age.goe(
                                select(memberSub.age.avg())
                                        .from(memberSub)))
                        .fetch();

        assertThat(result)
                .extracting("age")
                .containsExactly(30, 40);
    }

    /**
     * 나이가 10살 초과인 회원
     */
    @Test
    public void subQueryIn() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result =
                jpaQueryFactory
                        .selectFrom(member)
                        .where(member.age.in(
                                select(memberSub.age)
                                        .from(memberSub)
                                        .where(memberSub.age.gt(10))))
                        .fetch();

        assertThat(result)
                .extracting("age")
                .containsExactly(20, 30, 40);
    }

    @Test
    public void selectSubQuery() {
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result =
                jpaQueryFactory
                        .select(member.username,
                                select(memberSub.age.avg())
                                        .from(memberSub))
                        .from(member)
                        .fetch();

        result.forEach(tuple -> System.out.println("tuple = " + tuple));
    }

    @Test
    public void basicCase() {
        List<String> result =
                jpaQueryFactory
                        .select(member.age
                                .when(10).then("10살")
                                .when(20).then("20살")
                                .otherwise("기타"))
                        .from(member)
                        .fetch();

        result.forEach(s -> System.out.println("s = " + s));
    }

    @Test
    public void complexCase() {
        List<String> result =
                jpaQueryFactory
                        .select(new CaseBuilder()
                                .when(member.age.between(0, 20)).then("0~20살")
                                .when(member.age.between(21, 30)).then("21~30살")
                                .otherwise("기타"))
                        .from(member)
                        .fetch();

        result.forEach(s -> System.out.println("s = " + s));
    }

    @Test
    public void constant() {
        List<Tuple> result =
                jpaQueryFactory
                        .select(member.username, Expressions.constant("A"))
                        .from(member)
                        .fetch();

        result.forEach(tuple -> System.out.println("tuple = " + tuple));
    }

    @Test
    public void concat() {
        // {username}_{age}
        List<String> result =
                jpaQueryFactory
                        .select(member.username.concat("_").concat(member.age.stringValue()))
                        .from(member)
                        .where(member.username.eq("member1"))
                        .fetch();

        result.forEach(s -> System.out.println("s = " + s));
    }

    @Test
    public void simpleProjection() {
        List<String> result =
                jpaQueryFactory
                        .select(member.username)
                        .from(member)
                        .fetch();

        result.forEach(s -> System.out.println("s = " + s));
    }

    @Test
    public void tupleProjection() {
        List<Tuple> result =
                jpaQueryFactory
                        .select(member.username, member.age)
                        .from(member)
                        .fetch();

        result.forEach(tuple -> {
            System.out.println("username = " + tuple.get(member.username));
            System.out.println("age = " + tuple.get(member.age));
        });
    }

    @Test
    public void findDtoByJPQL() {
        List<MemberRecordDto> result =
                entityManager
                        .createQuery(
                                "select new study.querydsl.domain.member.dto.MemberRecordDto(m.username, m.age)" +
                                        " from Member m", MemberRecordDto.class)
                        .getResultList();

        result.forEach(MemberRecordDto -> System.out.println("memberDto = " + MemberRecordDto));
    }

    @Test
    public void findDtoBySetter() {
        List<MemberClassDto> result =
                jpaQueryFactory
                        .select(Projections.bean(
                                MemberClassDto.class,
                                member.username,
                                member.age))
                        .from(member)
                        .fetch();

        result.forEach(MemberClassDto -> System.out.println("memberDto = " + MemberClassDto));
    }

    @Test
    public void findDtoByField() {
        List<MemberClassDto> result =
                jpaQueryFactory
                        .select(Projections.fields(
                                MemberClassDto.class,
                                member.username,
                                member.age))
                        .from(member)
                        .fetch();

        result.forEach(MemberClassDto -> System.out.println("memberDto = " + MemberClassDto));
    }

    @Test
    public void findUserDtoByField() {
        QMember memberSub = new QMember("memberSub");

        List<UserDto> result =
                jpaQueryFactory
                        .select(Projections.fields(
                                UserDto.class,
                                member.username.as("name"),
                                ExpressionUtils.as(
                                        select(memberSub.age.max())
                                                .from(memberSub), "age")))
                        .from(member)
                        .fetch();

        result.forEach(UserDto -> System.out.println("userDto = " + UserDto));
    }

    @Test
    public void findDtoByConstructor() {
        List<MemberClassDto> result =
                jpaQueryFactory
                        .select(Projections.constructor(
                                MemberClassDto.class,
                                member.username,
                                member.age))
                        .from(member)
                        .fetch();

        result.forEach(MemberClassDto -> System.out.println("memberDto = " + MemberClassDto));
    }

    @Test
    public void findUserDtoByConstructor() {
        List<UserDto> result =
                jpaQueryFactory
                        .select(Projections.constructor(
                                UserDto.class,
                                member.username,
                                member.age))
                        .from(member)
                        .fetch();

        result.forEach(UserDto -> System.out.println("userDto = " + UserDto));
    }

    @Test
    public void findDtoByQueryProjection() {
        List<MemberRecordDto> result =
                jpaQueryFactory
                        .select(new QMemberRecordDto(member.username, member.age))
                        .from(member)
                        .fetch();

        result.forEach(MemberRecordDto -> System.out.println("memberRecordDto = " + MemberRecordDto));
    }
}