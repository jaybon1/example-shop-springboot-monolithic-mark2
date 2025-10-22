package com.example.shopmark2.global.presentation.dto;

import com.example.shopmark2.global.infrastructure.constants.Constants;
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
