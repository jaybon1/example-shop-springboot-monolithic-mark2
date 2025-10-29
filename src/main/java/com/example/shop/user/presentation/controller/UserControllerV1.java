package com.example.shop.user.presentation.controller;

import com.example.shop.common.infrastructure.config.security.auth.CustomUserDetails;
import com.example.shop.common.presentation.dto.ApiDto;
import com.example.shop.user.application.service.UserServiceV1;
import com.example.shop.user.presentation.dto.response.ResGetUsersDtoV1;
import com.example.shop.user.presentation.dto.response.ResGetUserDtoV1;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1/users")
public class UserControllerV1 {

    private final UserServiceV1 userServiceV1;

    @GetMapping
    public ResponseEntity<ApiDto<ResGetUsersDtoV1>> getUsers(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "email", required = false) String email
    ) {
        ResGetUsersDtoV1 responseBody = userServiceV1.getUsers(
                customUserDetails.getId(),
                customUserDetails.getRoleList(),
                pageable,
                username,
                nickname,
                email
        );
        return ResponseEntity.ok(
                ApiDto.<ResGetUsersDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiDto<ResGetUserDtoV1>> getUser(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable("id") UUID userId
    ) {
        return ResponseEntity.ok(
                ApiDto.<ResGetUserDtoV1>builder()
                        .data(userServiceV1.getUser(
                                customUserDetails.getId(),
                                customUserDetails.getRoleList(),
                                userId
                        ))
                        .build()
        );

    }

//    @PutMapping("/{id}")
//    public ResponseEntity<ApiDto<Object>> putMembersById(
//            @AuthenticationPrincipal CustomUserDetails customUserDetails,
//            @PathVariable("id") Long id
//    ) {
//
//        ApiDto<Object> apiDto = ApiDto.<Object>builder()
//                .code(0)
//                .message(id + " 번 회원 정보 업데이트 성공")
//                .build();
//
//        return ResponseEntity.ok(apiDto);
//
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiDto<Object>> deleteUser(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable("id") UUID userId
    ) {
        userServiceV1.deleteUser(
                customUserDetails.getId(),
                customUserDetails.getRoleList(),
                userId
        );
        return ResponseEntity.ok(
                ApiDto.builder()
                        .message("회원 삭제에 성공했습니다.")
                        .build()
        );

    }

}
