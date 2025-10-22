package com.example.shopmark2.global.infrastructure.config.security.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.shopmark2.user.domain.model.User;
import com.example.shopmark2.user.domain.model.UserRole;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomUserDetails implements UserDetails, OAuth2User {

    private UserPrincipal user;
    private Map<String, Object> attributes;

    //    public static CustomUserDetails of(MemberEntity memberEntity, Map<String, Object> attributes) {
//        return CustomUserDetails.builder()
//                .member(Member.from(memberEntity))
//                .attributes(attributes)
//                .build();
//    }
//
    public static CustomUserDetails of(User user) {
        return CustomUserDetails.builder()
                .user(UserPrincipal.from(user))
                .attributes(Map.of())
                .build();
    }

    public static CustomUserDetails of(DecodedJWT decodedAccessJwt) {
        return CustomUserDetails.builder()
                .user(UserPrincipal.from(decodedAccessJwt))
                .attributes(Map.of())
                .build();
    }

    @Getter
    @Builder
    public static class UserPrincipal {
        private UUID id;
        private String username;
        private String password;
        private String nickname;
        private String email;
        private List<String> roleList;

        public static UserPrincipal from(DecodedJWT decodedAccessJwt) {
            return UserPrincipal.builder()
                    .id(UUID.fromString(decodedAccessJwt.getClaim("id").asString()))
                    .username(decodedAccessJwt.getClaim("username").asString())
                    .password(null)
                    .nickname(String.valueOf(decodedAccessJwt.getClaim("nickname")))
                    .email(String.valueOf(decodedAccessJwt.getClaim("email")))
                    .roleList(decodedAccessJwt.getClaim("roleList").asList(String.class))
                    .build();
        }

        public static UserPrincipal from(User user) {
            return UserPrincipal.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .roleList(
                            user.getUserRoleList()
                                    .stream()
                                    .map(UserRole::getRole)
                                    .map(Enum::toString)
                                    .toList()
                    )
                    .build();
        }
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoleList()
                .stream()
                .map(role -> (GrantedAuthority) () -> "ROLE_" + role)
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

