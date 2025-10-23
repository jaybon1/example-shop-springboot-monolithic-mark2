package com.example.shop.global.infrastructure.config.security;


import com.example.shop.global.infrastructure.config.oauth2.CustomOAuth2AuthenticationFailureHandler;
import com.example.shop.global.infrastructure.config.oauth2.CustomOAuth2AuthenticationSuccessHandler;
import com.example.shop.global.infrastructure.config.security.auth.CustomAccessDeniedHandler;
import com.example.shop.global.infrastructure.config.security.auth.CustomAuthenticationEntryPoint;
import com.example.shop.global.infrastructure.config.security.auth.JwtAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Nullable
    @Value("${spring.profiles.active}")
    String activeProfile;

    private final CustomOAuth2AuthenticationSuccessHandler customOAuth2AuthenticationSuccessHandler;
    private final CustomOAuth2AuthenticationFailureHandler customOAuth2AuthenticationFailureHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedHeaders(Collections.singletonList("*"));
            config.setAllowedMethods(Collections.singletonList("*"));
            config.setAllowedOriginPatterns(List.of(
                    "http://127.0.0.1:[*]",
                    "http://localhost:[*]"
            ));
            config.setAllowCredentials(true);
            return config;
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);

        if ("dev".equals(activeProfile)) {
            httpSecurity.authorizeHttpRequests(config -> config
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/h2/**"))
                    .permitAll()
            );
        } else {
            httpSecurity.authorizeHttpRequests(config -> config
                    .requestMatchers(AntPathRequestMatcher.antMatcher("/h2/**"))
                    .hasRole("ADMIN")
            );
        }

        httpSecurity.headers(config -> config.frameOptions(frameOptionsConfig -> frameOptionsConfig.disable()));

        httpSecurity.cors(config -> config.configurationSource(corsConfigurationSource()));

        httpSecurity.csrf(config -> config.disable());

        httpSecurity.formLogin(config -> config.disable());

        httpSecurity.httpBasic(config -> config.disable());

        httpSecurity.oauth2Login(config -> config
                .loginPage("/auth/login")
                .successHandler(customOAuth2AuthenticationSuccessHandler)
                .failureHandler(customOAuth2AuthenticationFailureHandler)
        );

        httpSecurity.sessionManagement(config -> config
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        httpSecurity.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        httpSecurity.exceptionHandling(config -> config
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
        );

        httpSecurity.authorizeHttpRequests(config -> config
                .requestMatchers(
                        mvcMatcherBuilder.pattern("/css/**"),
                        mvcMatcherBuilder.pattern("/js/**"),
                        mvcMatcherBuilder.pattern("/assets/**"),
                        mvcMatcherBuilder.pattern("/springdoc/**"),
                        mvcMatcherBuilder.pattern("/favicon.ico")
                )
                .permitAll()
                .requestMatchers(
                        mvcMatcherBuilder.pattern("/js/admin*.js")
                )
                .hasRole("ADMIN")
        );

        httpSecurity.authorizeHttpRequests(config -> config
                .requestMatchers(
                        mvcMatcherBuilder.pattern("/docs/**"),
                        mvcMatcherBuilder.pattern("/swagger-ui/**"),
                        mvcMatcherBuilder.pattern("/auth/**"),
                        mvcMatcherBuilder.pattern("/oauth2/**"),
                        mvcMatcherBuilder.pattern("/login/oauth2/**"),
                        mvcMatcherBuilder.pattern("/oauth2/authorization/**"),
                        mvcMatcherBuilder.pattern("/v*/auth/**"),
                        mvcMatcherBuilder.pattern("/v*/users/**"),
                        mvcMatcherBuilder.pattern(HttpMethod.GET, "/v*/products/**")

                )
                .permitAll()
                .anyRequest()
                .authenticated()
        );

        return httpSecurity.getOrBuild();
    }

}
