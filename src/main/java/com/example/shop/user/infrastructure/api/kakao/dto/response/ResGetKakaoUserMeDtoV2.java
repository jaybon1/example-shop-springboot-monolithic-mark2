package com.example.shop.user.infrastructure.api.kakao.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResGetKakaoUserMeDtoV2 {

    private Long id;
    private KakaoAccount kakao_account;

    @Getter
    @Builder
    public static class KakaoAccount {

        private String email;
        private Profile profile;

        @Getter
        @Builder
        public static class Profile {

            private String nickname;

        }

    }

}
