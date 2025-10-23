package com.example.shop.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReqAuthPostRefreshDtoV1 {

    @NotBlank(message = "리프레시 토큰이 없습니다.")
    private String refreshJwt;

}
