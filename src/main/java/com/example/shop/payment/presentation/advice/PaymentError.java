package com.example.shop.payment.presentation.advice;

import com.example.shop.global.presentation.advice.GlobalError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentError implements GlobalError {

    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 내역을 찾을 수 없습니다."),
    PAYMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "결제 내역에 접근할 수 없습니다."),
    PAYMENT_ORDER_NOT_FOUND(HttpStatus.BAD_REQUEST, "주문 정보를 찾을 수 없습니다."),
    PAYMENT_ORDER_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 주문에 대해 결제할 수 없습니다."),
    PAYMENT_ORDER_CANCELLED(HttpStatus.BAD_REQUEST, "취소된 주문은 결제할 수 없습니다."),
    PAYMENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 결제 처리가 완료된 주문입니다."),
    PAYMENT_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "이미 취소된 결제입니다."),
    PAYMENT_USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "결제 사용자 정보를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getErrorCode() {
        return this.toString();
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
