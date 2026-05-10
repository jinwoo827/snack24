package com.snack24.identity.domain;

import com.snack24.common.jpabase.config.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@ToString
@Table(name = "companies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {
    @Id
    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "business_no", length = 12)
    private String businessNo;

    @Column(name = "business_no_digits", insertable = false, updatable = false, length = 10)
    private String businessNoDigits;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false, length = 20)
    private CompanyPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length =  20)
    private CompanyStatus status;

    @Column(name = "joined_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime joinedAt;

    public static Company register(Long companyId, String name, String businessNo, CompanyPlan plan, LocalDateTime joinedAt) {
        Company c = new Company();
        c.companyId = companyId;
        c.name = name;
        c.businessNo = businessNo;
        c.plan = plan;
        c.joinedAt = joinedAt;
        return c;
    }

    public void changePlan(CompanyPlan newPlan) {

        if (this.status == CompanyStatus.TERMINATED) {
            throw new IllegalStateException("terminated company cannot change plan");
        }
        this.plan = newPlan;
    }

    public void suspend() {
        this.status = CompanyStatus.SUSPENDED;
    }

    public void terminate() {
        this.status = CompanyStatus.TERMINATED;
    }

}
