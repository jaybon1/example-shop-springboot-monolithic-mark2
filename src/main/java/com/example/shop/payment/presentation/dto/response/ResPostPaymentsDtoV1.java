package com.example.shop.payment.presentation.dto.response;

import com.example.shop.order.domain.model.Order;
import com.example.shop.payment.domain.model.Payment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPostPaymentsDtoV1 {

    private PaymentDto payment;

    public static ResPostPaymentsDtoV1 of(Payment payment, Order order) {
        return ResPostPaymentsDtoV1.builder()
                .payment(PaymentDto.from(payment, order))
                .build();
    }

    @Getter
    @Builder
    public static class PaymentDto {

        private String id;
        private Payment.Status status;
        private Payment.Method method;
        private Long amount;
        private String transactionKey;
        private String orderId;
        private Order.Status orderStatus;

        public static PaymentDto from(Payment payment, Order order) {
            return PaymentDto.builder()
                    .id(payment.getId() != null ? payment.getId().toString() : null)
                    .status(payment.getStatus())
                    .method(payment.getMethod())
                    .amount(payment.getAmount())
                    .transactionKey(payment.getTransactionKey())
                    .orderId(order.getId() != null ? order.getId().toString() : null)
                    .orderStatus(order.getStatus())
                    .build();
        }
    }
}
