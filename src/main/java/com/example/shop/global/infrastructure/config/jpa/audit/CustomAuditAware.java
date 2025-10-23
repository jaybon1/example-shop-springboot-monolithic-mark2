package com.example.shop.global.infrastructure.config.jpa.audit;

import com.example.shop.global.infrastructure.config.security.auth.CustomUserDetails;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomAuditAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 1) 시스템(스케줄러/배치 등): SecurityContext 없음
        if (authentication == null) {
            return Optional.of("system");
        }

        // 2) 익명 사용자
        if (authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of("anonymous");
        }

        // 3) 인증 객체는 있으나 아직 인증 상태가 아님(드묾)
        if (!authentication.isAuthenticated()) {
            return Optional.of("custom_authentication");
        }

        // 4) 실제 사용자
        if (authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            return Optional.of(customUserDetails.getUser().getId().toString());
        }

        // 그 외
        return Optional.of("etc");
    }

}