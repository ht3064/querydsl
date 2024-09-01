package study.querydsl.domain.member.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import study.querydsl.domain.member.domain.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    List<Member> findByUsername(String username);
}