package com.example.shop.global.infrastructure.config.oauth2;

import com.example.shop.global.infrastructure.config.security.auth.CustomUserDetails;
import com.example.shop.global.infrastructure.constants.Constants;
import com.example.shop.global.util.UtilFunction;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessJwt = UtilFunction.generateAccessJwtBy(customUserDetails);
        String refreshJwt = UtilFunction.generateRefreshJwtBy(customUserDetails);
        String target = UriComponentsBuilder
                .fromUriString("/auth/social-result")
                .build()
                .toUriString();
        response.addHeader(Constants.Jwt.ACCESS, accessJwt);
        response.addHeader(Constants.Jwt.REFRESH, refreshJwt);
        try {
            request.getRequestDispatcher(target).forward(request, response);
        } catch (Exception e) {
            throw new RuntimeException("소셜 로그인에 실패했습니다. 반복적으로 실패 시 문의 바랍니다. " + e.getMessage());
        }
    }

}
