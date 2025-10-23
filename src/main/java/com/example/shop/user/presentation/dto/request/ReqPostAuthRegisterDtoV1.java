package com.example.shop.user.presentation.dto.request;

import com.example.shop.common.infrastructure.constants.Constants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReqPostAuthRegisterDtoV1 {

    @NotNull(message = "회원 정보를 입력해주세요.")
    @Valid
    private UserDto user;

    @Getter
    @Builder
    public static class UserDto {

        @NotBlank(message = "아이디를 입력해주세요.")
        @Pattern(regexp = Constants.Regex.USERNAME, message = "아이디는 3~20자의 영소문자, 숫자, 언더바(_)만으로 구성해주세요.")
        private String username;

        @NotBlank(message = "비밀번호를 입력해주세요.")
        private String password;

        @NotBlank(message = "닉네임을 입력해주세요.")
        private String nickname;

        @NotBlank(message = "이메일을 입력해주세요.")
        private String email;

    }

}
