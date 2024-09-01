package study.querydsl.domain.member.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record MemberTeamDto(Long memberId, String username, int age, Long teamId, String teamName) {

    @QueryProjection
    public MemberTeamDto(Long memberId, String username, int age, Long teamId, String teamName) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }
}