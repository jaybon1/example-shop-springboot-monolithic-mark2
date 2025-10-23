package com.example.shop.user.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPostAuthSocialDtoV1 {

    private String accessJwt;
    private String refreshJwt;

    public static ResPostAuthSocialDtoV1 of(String accessJwt, String refreshJwt) {
        return ResPostAuthSocialDtoV1.builder()
                .accessJwt(accessJwt)
                .refreshJwt(refreshJwt)
                .build();
    }

}
