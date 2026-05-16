package com.snack24.identity.config;

import com.snack24.identity.auth.MemberPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityAuditorAware implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .filter(authentication -> authentication.getPrincipal() instanceof MemberPrincipal)
                .map(authentication -> ((MemberPrincipal) authentication.getPrincipal()).memberId());
    }
}
