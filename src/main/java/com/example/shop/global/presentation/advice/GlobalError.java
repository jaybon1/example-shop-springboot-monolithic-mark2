package com.example.shop.global.presentation.advice;

import org.springframework.http.HttpStatus;

public interface GlobalError {

    HttpStatus getHttpStatus();

    String getErrorCode();

    String getErrorMessage();

}
