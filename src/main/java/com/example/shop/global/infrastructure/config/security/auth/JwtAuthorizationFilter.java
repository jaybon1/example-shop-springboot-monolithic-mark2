package com.example.shop.global.infrastructure.config.security.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.shop.global.infrastructure.constants.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    // 토큰 정보만으로 CustomUserDetails를 생성하는 로직을 추가하여, UserDetailsService를 사용하지 않도록 변경

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        String accessJWTHeader = request.getHeader(Constants.Jwt.ACCESS_HEADER_NAME);
        DecodedJWT decodedAccessJWT;
        if (accessJWTHeader == null || !accessJWTHeader.startsWith(Constants.Jwt.HEADER_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }
        String accessJWT = accessJWTHeader.replaceFirst(Constants.Jwt.HEADER_PREFIX, "");
        try {
            decodedAccessJWT = JWT.require(Algorithm.HMAC512(Constants.Jwt.SECRET))
                    .build()
                    .verify(accessJWT);
        } catch (JWTVerificationException e) {
            chain.doFilter(request, response);
            return;
        }
        if (decodedAccessJWT.getExpiresAtAsInstant().isBefore(Instant.now())) {
            chain.doFilter(request, response);
            return;
        }
        if (!"accessJwt".equals(decodedAccessJWT.getSubject())) {
            chain.doFilter(request, response);
            return;
        }
        CustomUserDetails customUserDetails;
        try {
            customUserDetails = CustomUserDetails.of(decodedAccessJWT);
        } catch (Exception e) {
            chain.doFilter(request, response);
            return;
        }
        // TODO : 추후 Redis를 이용한 JWT 검증 로직 추가
//        if (Instant.ofEpochMilli(customUserDetails.getMember().getJwtValidator()).isAfter(decodedAccessJWT.getIssuedAtAsInstant())) {
//            chain.doFilter(request, response);
//            return;
//        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        customUserDetails,
                        null,
                        customUserDetails.getAuthorities()
                )
        );
        chain.doFilter(request, response);

    }
}
