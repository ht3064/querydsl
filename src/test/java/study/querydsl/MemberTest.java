package study.querydsl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.member.domain.Member;
import study.querydsl.domain.team.domain.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.domain.team.domain.Team.createTeam;

@SpringBootTest
@Transactional
class MemberTest {

    @PersistenceContext
    EntityManager entityManager;

    @Test
    public void 회원_테스트() {
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

        // 초기화
        entityManager.flush();
        entityManager.clear();

        List<Member> members = entityManager
                .createQuery("select m from Member m", Member.class)
                .getResultList();

        assertThat(members.size()).isEqualTo(4);
    }
}