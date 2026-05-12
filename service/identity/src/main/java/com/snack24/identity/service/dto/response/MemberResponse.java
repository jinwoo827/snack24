package com.snack24.identity.service.dto.response;

import com.snack24.identity.domain.Member;
import com.snack24.identity.domain.MemberRole;
import com.snack24.identity.domain.MemberStatus;

import java.time.LocalDateTime;

public record MemberResponse(
        Long memberId,
        Long companyId,
        Long departmentId,
        String email,
        String name,
        MemberRole role,
        MemberStatus status,
        LocalDateTime joinedAt
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getMemberId(),
                member.getCompanyId(),
                member.getDepartmentId(),
                member.getEmail(),
                member.getName(),
                member.getRole(),
                member.getStatus(),
                member.getJoinedAt()
        );
    }
}
