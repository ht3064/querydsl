package study.querydsl.domain.member.dto.request;

public record MemberSearchCondition(String username, String teamName, Integer ageGoe, Integer ageLoe) {
    public static MemberSearchCondition of(String username, String teamName, Integer ageGoe, Integer ageLoe) {
        return new MemberSearchCondition(username, teamName, ageGoe, ageLoe);
    }
}