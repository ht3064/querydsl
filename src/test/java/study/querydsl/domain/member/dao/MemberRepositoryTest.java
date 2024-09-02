package study.querydsl.domain.member.dao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.member.domain.Member;
import study.querydsl.domain.member.dto.request.MemberSearchCondition;
import study.querydsl.domain.member.dto.response.MemberTeamDto;
import study.querydsl.domain.member.dto.response.MemberTeamDtoV2;
import study.querydsl.domain.team.domain.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.domain.member.domain.Member.createMember;
import static study.querydsl.domain.team.domain.Team.createTeam;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void basicTest() {
        Member member = createMember("member1", 10, null);
        memberRepository.save(member);

        Member findMember =
                memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    public void searchTest() {
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

        MemberSearchCondition condition =
                MemberSearchCondition.of(null, "teamB", 35, 40);

        List<MemberTeamDto> result = memberRepository.search(condition);

        assertThat(result)
                .extracting("username")
                .containsExactly("member4");
    }

    @Test
    public void searchPage() {
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

        MemberSearchCondition condition =
                MemberSearchCondition.of(null, null, null, null);

        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDto> result = memberRepository.searchPage(condition, pageRequest);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent())
                .extracting("username")
                .containsExactly("member1", "member2", "member3");
    }

    @Test
    public void searchSlice() {
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

        MemberSearchCondition condition =
                MemberSearchCondition.of(null, null, null, null);

        Slice<MemberTeamDtoV2> result =
                memberRepository.searchSlice(condition, null, 2);

        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.isLast()).isFalse();
        assertThat(result)
                .extracting("memberId")
                .containsExactly(4L, 3L);

        Slice<MemberTeamDtoV2> lastResult =
                memberRepository.searchSlice(condition, 3L, 2);

        assertThat(lastResult.getSize()).isEqualTo(2);
        assertThat(lastResult.hasNext()).isFalse();
        assertThat(lastResult.isLast()).isTrue();
        assertThat(lastResult)
                .extracting("memberId")
                .containsExactly(2L, 1L);
    }
}