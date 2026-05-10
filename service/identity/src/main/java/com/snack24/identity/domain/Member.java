package com.snack24.identity.domain;

import com.snack24.common.jpabase.config.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Id
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status;

    @Column(name = "joined_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime joinedAt;

    @Column(name = "last_login_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime lastLoginAt;

    public static Member register(Long memberId, Long companyId, Long departmentId, String email, String passwordHash, String name,
                                  String phone, MemberRole role, LocalDateTime joinedAt) {
        Member member = new Member();
        member.memberId = memberId;
        member.companyId = companyId;
        member.departmentId = departmentId;
        member.email = email;
        member.passwordHash = passwordHash;
        member.name = name;
        member.phone = phone;
        member.role = role;
        member.joinedAt = joinedAt;
        member.status = MemberStatus.ACTIVE;
        return member;
    }

    public void changeDepartment(Long newDepartmentId) {
        this.departmentId = newDepartmentId;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void recordLogin(LocalDateTime at) {
        this.lastLoginAt = at;
    }

    public void leave() {
        if (this.status == MemberStatus.LEFT) {
            throw new IllegalStateException("already left");
        }
        this.status = MemberStatus.LEFT;
    }
}
