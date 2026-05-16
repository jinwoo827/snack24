package com.snack24.identity.controller;

import com.snack24.common.jpabase.dto.response.PageResponse;
import com.snack24.identity.auth.MemberPrincipal;
import com.snack24.identity.exception.IdentityErrorCode;
import com.snack24.identity.exception.IdentityException;
import com.snack24.identity.repository.MemberRepository;
import com.snack24.identity.repository.dto.MemberListItem;
import com.snack24.identity.repository.dto.MemberSearchCondition;
import com.snack24.identity.service.MemberService;
import com.snack24.identity.service.dto.request.MemberRegisterRequest;
import com.snack24.identity.service.dto.response.MemberResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/v1/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_COMPANY_ADMIN')")
    public ResponseEntity<MemberResponse> register(
            @RequestBody @Valid MemberRegisterRequest request,
            @AuthenticationPrincipal MemberPrincipal me
    ) {
        MemberRegisterRequest tenantSafe = request.withCompanyId(me.companyId());
        MemberResponse response = memberService.register(tenantSafe);
        URI location = URI.create("/v1/members/" + response.memberId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public PageResponse<MemberListItem> search(
            @ModelAttribute MemberSearchCondition cond,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal MemberPrincipal me
    ) {
        MemberSearchCondition tenantSafe = cond.withCompanyId(me.companyId());
        return PageResponse.from(memberRepository.searchAdmin(tenantSafe, pageable));
    }

    @GetMapping("/me")
    public MemberResponse me(@AuthenticationPrincipal MemberPrincipal me) {
        return memberService.get(me.memberId(), me.companyId());
    }

    @GetMapping("/{memberId}")
    public MemberResponse get(@PathVariable Long memberId, @AuthenticationPrincipal MemberPrincipal me) {
        return memberService.get(memberId, me.companyId());
    }
}
