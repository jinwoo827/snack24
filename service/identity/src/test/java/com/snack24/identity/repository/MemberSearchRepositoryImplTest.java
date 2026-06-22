package com.snack24.identity.repository;

import com.snack24.identity.domain.MemberStatus;
import com.snack24.identity.repository.dto.MemberListItem;
import com.snack24.identity.repository.dto.MemberSearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
@Slf4j
class MemberSearchRepositoryImplTest {

    @Autowired MemberRepository memberRepository;

    /*@Test
    void searchTest() {
        MemberSearchCondition cond = MemberSearchCondition.builder()
                .companyId(1L)
                .name("홍")
                .status(MemberStatus.ACTIVE)
                .build();

        Page<MemberListItem> page = memberRepository.searchAdmin(cond, PageRequest.of(0, 10));
        for (MemberListItem memberListItem : page.getContent()) {
            log.info("member = {}", memberListItem);
        }

    }*/
}