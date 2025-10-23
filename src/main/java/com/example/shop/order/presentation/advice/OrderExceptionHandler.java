package com.example.shop.order.presentation.advice;

import com.example.shop.common.presentation.dto.ApiDto;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(0)
public class OrderExceptionHandler {

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ApiDto<Object>> handleOrderException(OrderException e) {
        return new ResponseEntity<>(
                ApiDto.builder()
                        .code(e.getError().getErrorCode())
                        .message(e.getError().getErrorMessage())
                        .build(),
                e.getError().getHttpStatus()
        );
    }
}

