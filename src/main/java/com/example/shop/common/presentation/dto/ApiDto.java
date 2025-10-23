package com.example.shop.common.presentation.dto;

import com.example.shop.common.infrastructure.constants.Constants;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiDto<T> {

    @Builder.Default
    private String code = Constants.ApiCode.SUCCESS.toString();

    @Builder.Default
    private String message = "success";

    private T data;

}
