package study.querydsl.domain.member.dto;

import com.querydsl.core.annotations.QueryProjection;

public record MemberRecordDto(String username, int age) {

    @QueryProjection
    public MemberRecordDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}