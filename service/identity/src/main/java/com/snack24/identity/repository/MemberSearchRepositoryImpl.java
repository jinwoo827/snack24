package com.snack24.identity.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.snack24.identity.domain.MemberRole;
import com.snack24.identity.domain.MemberStatus;
import com.snack24.identity.repository.dto.MemberListItem;
import com.snack24.identity.repository.dto.MemberSearchCondition;
import com.snack24.identity.repository.dto.QMemberListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

import static com.snack24.identity.domain.QDepartment.department;
import static com.snack24.identity.domain.QMember.member;

@RequiredArgsConstructor
public class MemberSearchRepositoryImpl implements MemberSearchRepository {

    private final JPAQueryFactory query;

    @Override
    public Page<MemberListItem> searchAdmin(MemberSearchCondition cond, Pageable pageable) {

        // contents
        List<MemberListItem> contents = query.select(new QMemberListItem(
                        member.memberId,
                        member.name,
                        member.email,
                        member.departmentId,
                        department.name,
                        member.role,
                        member.status,
                        member.joinedAt
                ))
                .from(member)
                .leftJoin(department).on(department.departmentId.eq(member.departmentId))
                .where(
                        companyIdEq(cond.getCompanyId()),
                        departmentScope(cond),
                        nameContains(cond.getName()),
                        emailContains(cond.getEmail()),
                        statusEq(cond.getStatus()),
                        roleEq(cond.getRole()),
                        joinedBetween(cond.getJoinedFrom(), cond.getJoinedTo())
                )
                .orderBy(member.createdAt.desc(), member.memberId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // count
        JPAQuery<Long> countQuery = query
                .select(member.count())
                .from(member)
                .where(
                        companyIdEq(cond.getCompanyId()),
                        departmentScope(cond),
                        nameContains(cond.getName()),
                        emailContains(cond.getEmail()),
                        statusEq(cond.getStatus()),
                        roleEq(cond.getRole()),
                        joinedBetween(cond.getJoinedFrom(), cond.getJoinedTo())
                );

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    private BooleanExpression companyIdEq(Long companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required");
        }
        return member.companyId.eq(companyId);
    }

    private BooleanExpression departmentScope(MemberSearchCondition cond) {
        if (cond.getDepartmentId() == null) return null;
        if (!cond.isIncludeDescendants()) {
            return member.departmentId.eq(cond.getDepartmentId());
        }
        throw new UnsupportedOperationException("includeDescendants not implemented yet");
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? member.name.contains(name) : null;
    }

    private BooleanExpression emailContains(String email) {
        return StringUtils.hasText(email) ? member.email.contains(email) : null;
    }

    private BooleanExpression statusEq(MemberStatus status) {
        return status != null ? member.status.eq(status) : null;
    }

    private BooleanExpression roleEq(MemberRole role) {
        return role != null ? member.role.eq(role) : null;
    }

    private BooleanExpression joinedBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;
        if (from != null && to != null) return member.joinedAt.between(from.atStartOfDay(), to.atTime(23,59,59,999_999_000));
        if (from != null) return member.joinedAt.goe(from.atStartOfDay());
        return member.joinedAt.loe(to.atTime(23, 59, 59, 999_999_000));
    }
}
