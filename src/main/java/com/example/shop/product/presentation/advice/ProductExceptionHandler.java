package com.example.shop.product.presentation.advice;

import com.example.shop.global.presentation.dto.ApiDto;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(0)
public class ProductExceptionHandler {

    @ExceptionHandler(ProductException.class)
    public ResponseEntity<ApiDto<Object>> handleProductException(ProductException e) {
        return new ResponseEntity<>(
                ApiDto.builder()
                        .code(e.getError().getErrorCode())
                        .message(e.getError().getErrorMessage())
                        .build(),
                e.getError().getHttpStatus()
        );
    }
}
