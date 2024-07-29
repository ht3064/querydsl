package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.member.domain.Member;
import study.querydsl.domain.team.domain.Team;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.domain.member.domain.QMember.*;
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
}