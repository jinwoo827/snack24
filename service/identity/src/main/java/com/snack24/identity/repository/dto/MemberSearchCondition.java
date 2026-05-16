package com.snack24.identity.repository.dto;

import com.snack24.identity.domain.MemberRole;
import com.snack24.identity.domain.MemberStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.LocalDate;

@Getter
@Builder
public class MemberSearchCondition {
    @With
    private Long companyId;
    private Long departmentId;
    private boolean includeDescendants;
    private String name;
    private String email;
    private MemberStatus status;
    private MemberRole role;
    private LocalDate joinedFrom;
    private LocalDate joinedTo;

}
