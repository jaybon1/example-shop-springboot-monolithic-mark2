package com.example.shop.user.presentation.controller;

import com.example.shop.common.presentation.dto.ApiDto;
import com.example.shop.user.application.service.AuthServiceV1;
import com.example.shop.user.presentation.dto.request.ReqAuthPostRefreshDtoV1;
import com.example.shop.user.presentation.dto.request.ReqPostAuthLoginDtoV1;
import com.example.shop.user.presentation.dto.request.ReqPostAuthRegisterDtoV1;
import com.example.shop.user.presentation.dto.response.ResPostAuthLoginDtoV1;
import com.example.shop.user.presentation.dto.response.ResPostAuthRefreshDtoV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1/auth")
public class AuthControllerV1 {

    private final AuthServiceV1 authServiceV1;

    @PostMapping("/register")
    public ResponseEntity<ApiDto<Object>> postAuthRegister(
            @RequestBody @Valid ReqPostAuthRegisterDtoV1 reqDto
    ) {

        authServiceV1.postAuthRegister(reqDto);
        return ResponseEntity.ok(
                ApiDto.builder()
                        .message("회원가입에 성공하였습니다.")
                        .build()
        );

    }

    @PostMapping("/login")
    public ResponseEntity<ApiDto<ResPostAuthLoginDtoV1>> postAuthLogin(
            @RequestBody @Valid ReqPostAuthLoginDtoV1 reqDto
    ) {

        return ResponseEntity.ok(
                ApiDto.<ResPostAuthLoginDtoV1>builder()
                        .message("로그인에 성공하였습니다.")
                        .data(authServiceV1.postAuthLogin(reqDto))
                        .build()
        );

    }

//    @PostMapping("/social")
//    public ResponseEntity<ApiDto<ResPostAuthSocialDtoApiV1>> postAuthSocial(
//            @RequestBody @Valid ReqPostAuthSocialDtoV1 reqDto
//    ) {
//
//        return ResponseEntity.ok(
//                ApiDto.<ResPostAuthSocialDtoApiV1>builder()
//                        .data(authServiceV1.postAuthSocial(reqDto))
//                        .build()
//        );
//
//    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiDto<ResPostAuthRefreshDtoV1>> postAuthRefresh(
            @RequestBody @Valid ReqAuthPostRefreshDtoV1 reqDto
    ) {

        return ResponseEntity.ok(
                ApiDto.<ResPostAuthRefreshDtoV1>builder()
                        .data(authServiceV1.postAuthRefresh(reqDto))
                        .build()
        );

    }

}
