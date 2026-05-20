package com.snack24.order.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Component
public class HeaderAuditorAware implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            return Optional.empty();
        }

        String memberId = attrs.getRequest().getHeader("X-Member-Id");
        if (memberId == null) {
            return Optional.empty();
        }

        return Optional.of(Long.valueOf(memberId));
    }
}
