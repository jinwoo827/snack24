package com.snack24.identity.controller;

import com.snack24.identity.auth.service.dto.request.LoginRequest;
import com.snack24.identity.auth.service.dto.response.LoginResponse;
import com.snack24.identity.domain.MemberRole;
import com.snack24.identity.service.dto.request.CompanyRegisterAdminRequest;
import com.snack24.identity.service.dto.request.CompanyRegisterRequest;
import com.snack24.identity.service.dto.request.MemberRegisterRequest;
import com.snack24.identity.service.dto.response.CompanyResponse;
import com.snack24.identity.service.dto.response.MemberResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class CompanyControllerTest {
    RestClient client = RestClient.create("http://localhost:8001");

    /*@Test
    void registerCompanyTest() {
        String password = "1q2w3e4r";
        CompanyResponse response = client.post()
                .uri("/v1/companies")
                .body(new CompanyRegisterRequest("회사3", "1104211113", new CompanyRegisterAdminRequest("jinwoo3@test.co.kr", password, "이진우", "01072729323")))
                .retrieve()
                .body(CompanyResponse.class);
        log.info("response = {}", response);
        String email = response.email();

        LoginResponse loginResponse = client.post()
                .uri("/v1/auth/login")
                .body(new LoginRequest(email, password))
                .retrieve()
                .body(LoginResponse.class);
        log.info("loginResponse = {}", loginResponse);

        MemberResponse meResponse = client.get()
                .uri("/v1/members/me")
                .header("Authorization", "Bearer " + loginResponse.accessToken())
                .retrieve()
                .body(MemberResponse.class);
        log.info("meResponse = {}", meResponse);
    }

    @Test
    void callMeApiIfWithoutToken() {
        MemberResponse body = client.get()
                .uri("/v1/members/me")
                .retrieve()
                .body(MemberResponse.class);
    }

    @Test
    void callRegisterApiIfNotAuthorityTest() {
        String email = "jinwoo1@test.co.kr";
        String password = "jinwoopwd";

        LoginResponse loginResponse = client.post()
                .uri("/v1/auth/login")
                .body(new LoginRequest(email, password))
                .retrieve()
                .body(LoginResponse.class);
        log.info("loginResponse = {}", loginResponse);

        MemberResponse body = client.post()
                .uri("/v1/members")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginResponse.accessToken())
                .body(new MemberRegisterRequest(1L, 1L, "null@test.co.kr", "pwdpwdpwd", "test","01072729323", MemberRole.ROLE_COMPANY_ADMIN))
                .retrieve()
                .body(MemberResponse.class);

    }*/

}