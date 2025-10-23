package com.example.shop.user.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResPostAuthRefreshDtoV1 {

    private String accessJwt;
    private String refreshJwt;

    public static ResPostAuthRefreshDtoV1 of(String accessJwt, String refreshJwt) {
        return ResPostAuthRefreshDtoV1.builder()
                .accessJwt(accessJwt)
                .refreshJwt(refreshJwt)
                .build();
    }

}
