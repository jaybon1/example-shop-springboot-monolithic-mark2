package com.example.shop.global.infrastructure.constants;

public class Constants {

    public static class Jwt {
        public static final String ACCESS = "accessJwt"; // 서버만 알고 있는 비밀값
        public static final String REFRESH = "refreshJwt"; // 서버만 알고 있는 비밀값
        public static final String SECRET = "sweetsalt"; // 서버만 알고 있는 비밀값
        public static final long ACCESS_EXPIRATION_TIME = 1000L * 30 * 60; // 만료기간 30분 (1/1000초)
        public static final long REFRESH_EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 180; // 만료기간 180일 (1/1000초)
        public static final String ACCESS_HEADER_NAME = "Authorization";
        public static final String HEADER_PREFIX = "Bearer ";
    }

    public static class Regex {
        public static final String USERNAME = "^[a-z0-9_]{3,20}$"; // 3~20자 영소문자, 숫자, 언더바(_)
        public static final String MARKDOWN = "(\\*\\*|\\*|_|#|`{1,3}|~{2}|-{3,}|>{1,}|\\[.*?\\]\\(.*?\\)|!\\[.*?\\]\\(.*?\\)|\\n)";
        public static final String MARKDOWN_IMAGE = "!\\[[^\\]]*\\]\\(([^)]+)\\)";
        public static final String VALID_IMAGE_EXTENSION = "([^\\s]+(\\.(?i)(jpg|jpeg|png|gif))$)";
    }

    public enum ApiCode {
        SUCCESS,
        MISSING_SERVLET_REQUEST_PARAMETER_EXCEPTION,
        BIND_EXCEPTION,
        CONSTRAINT_VIOLATION_EXCEPTION,
        HTTP_MESSAGE_NOT_READABLE_EXCEPTION,
        HTTP_REQUEST_METHOD_NOT_SUPPORT_EXCEPTION,
        METHOD_ARGUMENT_TYPE_MISMATCH_EXCEPTION,
        CONVERSION_FAILED_EXCEPTION,
        EXCEPTION;
    }

}
