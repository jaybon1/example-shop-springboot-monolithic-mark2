package com.example.shopmark2.payment.presentation.advice;

import com.example.shopmark2.global.presentation.advice.GlobalError;
import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {

    private final GlobalError error;

    public PaymentException(PaymentError error) {
        super(error.getErrorMessage());
        this.error = error;
    }
}

