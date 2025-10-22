package com.example.shopmark2.user.presentation.dto.response;

import com.example.shopmark2.user.domain.model.User;
import com.example.shopmark2.user.domain.model.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResGetUsersWithIdDtoV1 {

    private UserDto user;

    public static ResGetUsersWithIdDtoV1 of(User user) {
        return ResGetUsersWithIdDtoV1.builder()
                .user(UserDto.from(user))
                .build();
    }

    @Getter
    @Builder
    public static class UserDto {

        private String id;
        private String username;
        private String nickname;
        private String email;
        private List<UserRoleDto> userRoleList;
//        private List<UserSocial> userSocialList;

        public static UserDto from(com.example.shopmark2.user.domain.model.User user) {

            return UserDto.builder()
                    .id(user.getId().toString())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .userRoleList(UserRoleDto.from(user.getUserRoleList()))
//                    .memberSocialList(MemberSocial.from(userEntity.getMemberSocialList()))
                    .build();
        }

        @Getter
        @Builder
        public static class UserRoleDto {

            private String id;
            private UserRole.Role role;

            public static List<UserRoleDto> from(List<UserRole> userRoleList) {
                return userRoleList.stream()
                        .map(UserRoleDto::from)
                        .toList();
            }

            public static UserRoleDto from(UserRole userRole) {
                return UserRoleDto.builder()
                        .id(userRole.getId() != null ? userRole.getId().toString() : null)
                        .role(userRole.getRole())
                        .build();
            }

        }

        @Getter
        @Builder
        public static class UserSocialDto {

            private String id;
//            private MemberSocialEntity.Provider provider;
//            private String providerId;
//            private String nickname;
//            private String email;
//
//            public static List<UserSocial> from(List<MemberSocialEntity> memberSocialEntityList) {
//                return memberSocialEntityList.stream()
//                        .map(UserSocial::from)
//                        .toList();
//            }
//
//            public static UserSocial from(MemberSocialEntity memberSocialEntity) {
//                return UserSocial.builder()
//                        .id(memberSocialEntity.getId())
//                        .provider(memberSocialEntity.getProvider())
//                        .providerId(memberSocialEntity.getProviderId())
//                        .nickname(memberSocialEntity.getNickname())
//                        .email(memberSocialEntity.getEmail())
//                        .build();
//            }

        }

    }

}
