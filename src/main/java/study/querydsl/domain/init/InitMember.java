package study.querydsl.domain.init;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.team.domain.Team;

import java.util.stream.IntStream;

import static study.querydsl.domain.member.domain.Member.createMember;
import static study.querydsl.domain.team.domain.Team.createTeam;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService initMemberService;

    @PostConstruct
    public void init() {
        initMemberService.init();
    }

    @Component
    static class InitMemberService {
        @PersistenceContext
        private EntityManager entityManager;

        @Transactional
        public void init() {
            Team teamA = createTeam("teamA");
            Team teamB = createTeam("teamB");
            entityManager.persist(teamA);
            entityManager.persist(teamB);

            IntStream.range(0, 100).forEach(
                    i -> {
                        Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                        entityManager.persist(createMember("member" + i, i, selectedTeam));
                    });
        }
    }
}