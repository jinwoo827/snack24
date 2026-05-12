package com.snack24.identity.controller;

import com.snack24.common.jpabase.dto.response.PageResponse;
import com.snack24.identity.repository.MemberRepository;
import com.snack24.identity.repository.dto.MemberListItem;
import com.snack24.identity.repository.dto.MemberSearchCondition;
import com.snack24.identity.service.MemberService;
import com.snack24.identity.service.dto.request.MemberRegisterRequest;
import com.snack24.identity.service.dto.response.MemberResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/v1/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @PostMapping
    public ResponseEntity<MemberResponse> register(
            @RequestBody @Valid MemberRegisterRequest request
    ) {
        MemberResponse response = memberService.register(request);
        URI location = URI.create("/v1/members/" + response.memberId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{memberId}")
    public MemberResponse get(@PathVariable Long memberId) {
        return memberService.get(memberId);
    }

    @GetMapping
    public PageResponse<MemberListItem> search(
            @ModelAttribute MemberSearchCondition cond,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
            ) {
        return PageResponse.from(memberRepository.searchAdmin(cond, pageable));
    }


}
