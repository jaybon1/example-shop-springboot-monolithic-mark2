package com.example.shop.payment.infrastructure.persistence.mapper;

import com.example.shop.payment.domain.model.Payment;
import com.example.shop.payment.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toDomain(PaymentEntity paymentEntity) {
        if (paymentEntity == null) {
            return null;
        }
        return Payment.builder()
                .id(paymentEntity.getId())
                .orderId(paymentEntity.getOrder() != null ? paymentEntity.getOrder().getId() : null)
                .userId(paymentEntity.getUser() != null ? paymentEntity.getUser().getId() : null)
                .status(toDomain(paymentEntity.getStatus()))
                .method(toDomain(paymentEntity.getMethod()))
                .amount(paymentEntity.getAmount())
                .transactionKey(paymentEntity.getTransactionKey())
                .createdAt(paymentEntity.getCreatedAt())
                .createdBy(paymentEntity.getCreatedBy())
                .updatedAt(paymentEntity.getUpdatedAt())
                .updatedBy(paymentEntity.getUpdatedBy())
                .deletedAt(paymentEntity.getDeletedAt())
                .deletedBy(paymentEntity.getDeletedBy())
                .build();
    }

    public PaymentEntity toEntity(Payment payment) {
        if (payment == null) {
            return null;
        }
        return PaymentEntity.builder()
                .id(payment.getId())
                .status(toEntity(payment.getStatus()))
                .method(toEntity(payment.getMethod()))
                .amount(payment.getAmount())
                .transactionKey(payment.getTransactionKey())
                .build();
    }

    public void applyDomain(Payment payment, PaymentEntity paymentEntity) {
        if (payment == null || paymentEntity == null) {
            return;
        }
        paymentEntity.updateDetails(
                toEntity(payment.getMethod()),
                payment.getAmount(),
                payment.getTransactionKey()
        );
        if (payment.getStatus() != null) {
            switch (payment.getStatus()) {
                case COMPLETED -> paymentEntity.markCompleted();
                case CANCELLED -> paymentEntity.markCancelled();
            }
        }
    }

    private Payment.Status toDomain(PaymentEntity.Status status) {
        if (status == null) {
            return null;
        }
        return Payment.Status.valueOf(status.name());
    }

    private Payment.Method toDomain(PaymentEntity.Method method) {
        if (method == null) {
            return null;
        }
        return Payment.Method.valueOf(method.name());
    }

    private PaymentEntity.Status toEntity(Payment.Status status) {
        if (status == null) {
            return null;
        }
        return PaymentEntity.Status.valueOf(status.name());
    }

    private PaymentEntity.Method toEntity(Payment.Method method) {
        if (method == null) {
            return null;
        }
        return PaymentEntity.Method.valueOf(method.name());
    }
}
