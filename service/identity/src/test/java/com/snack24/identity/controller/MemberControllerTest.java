package com.snack24.identity.controller;

import com.snack24.identity.auth.*;
import com.snack24.identity.config.SecurityConfig;
import com.snack24.identity.domain.MemberRole;
import com.snack24.identity.repository.MemberRepository;
import com.snack24.identity.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(controllers = MemberController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class,
        TokenProvider.class})
@TestPropertySource(properties ={
        "snack24.jwt.issuer=snack24-identity",
        "snack24.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWxvY2FsLW9ubHktMzItYnl0ZS1taW4=",
        "snack24.jwt.access-token-validity=PT30M",
        "snack24.jwt.refresh-token-validity=P14D"
})
@EnableConfigurationProperties(JwtProperties.class)
@Slf4j
class MemberControllerTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    TokenProvider tokenProvider;
    @MockBean
    MemberService memberService;
    @MockBean
    MemberRepository memberRepository;

    @Test
    void callApiIfNoAuthenticatedThen401() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/v1/members/me"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("AUTH_REQUIRED"));
    }

    @Test
    void callApiIfInvalidTokenTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/v1/members/me").header("Authorization", "Bearer invalid"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void callApiIfValidTokenTest() throws Exception {
        String token = tokenProvider.issueAccessToken(11L, 1L, MemberRole.ROLE_MEMBER);
        log.info("token = {}", token);
        BDDMockito.given(memberService.get(11L)).willReturn(null);
        mvc.perform(MockMvcRequestBuilders.get("/v1/members/me").header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}