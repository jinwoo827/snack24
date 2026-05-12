package com.snack24.identity.repository;

import com.snack24.identity.repository.dto.MemberListItem;
import com.snack24.identity.repository.dto.MemberSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberSearchRepository {
    Page<MemberListItem> searchAdmin(MemberSearchCondition cond, Pageable pageable);
}
