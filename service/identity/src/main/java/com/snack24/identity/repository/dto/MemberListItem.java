package com.snack24.identity.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.snack24.identity.domain.MemberRole;
import com.snack24.identity.domain.MemberStatus;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class MemberListItem {
    private final Long memberId;
    private final String name;
    private final String email;
    private final Long departmentId;
    private final String departmentName;
    private final MemberRole role;
    private final MemberStatus status;
    private final LocalDateTime joinedAt;

    @QueryProjection
    public MemberListItem(Long memberId, String name, String email, Long departmentId, String departmentName, MemberRole role, MemberStatus status,
                          LocalDateTime joinedAt) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.role = role;
        this.status = status;
        this.joinedAt = joinedAt;
    }
}
