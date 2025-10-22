package com.example.shopmark2.global.presentation.advice;

import org.springframework.http.HttpStatus;

public interface GlobalError {

    HttpStatus getHttpStatus();

    String getErrorCode();

    String getErrorMessage();

}
