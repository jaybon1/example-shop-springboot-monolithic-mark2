package com.example.shopmark2.payment.domain.repository;

import com.example.shopmark2.payment.domain.model.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID paymentId);

    long count();
}
