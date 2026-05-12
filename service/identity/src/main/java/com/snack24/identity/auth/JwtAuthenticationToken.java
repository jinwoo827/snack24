package com.snack24.identity.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final MemberPrincipal principal;

    public JwtAuthenticationToken(MemberPrincipal principal) {
        super(List.of(new SimpleGrantedAuthority(principal.roleName())));
        this.principal = principal;
        setAuthenticated(true);
    }

    // 토큰 정보 보관 안함
    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
