package com.example.shopmark2.payment.presentation.dto.request;

import com.example.shopmark2.payment.domain.model.Payment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ReqPostPaymentsDtoV1 {

    @NotNull(message = "결제 정보를 입력해주세요.")
    @Valid
    private PaymentDto payment;

    @Getter
    @Builder
    public static class PaymentDto {

        @NotNull(message = "주문 ID를 입력해주세요.")
        private UUID orderId;

        @NotNull(message = "결제 수단을 입력해주세요.")
        private Payment.Method method;

        private String transactionKey;
    }
}
