package com.snack24.identity.controller;

import com.snack24.identity.service.CompanyService;
import com.snack24.identity.service.dto.request.CompanyRegisterRequest;
import com.snack24.identity.service.dto.response.CompanyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/v1/companies")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {
    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<CompanyResponse> register(
            @RequestBody @Valid CompanyRegisterRequest request
    ) {
        CompanyResponse response = companyService.register(request);
        URI location = URI.create("/v1/companies/" + response.companyId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{companyId}")
    public CompanyResponse get(@PathVariable Long companyId) {
        return companyService.get(companyId);
    }
}
