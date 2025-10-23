package com.example.shop.payment.domain.model;

import java.util.function.Consumer;

/**
 * Test-only helper to clone {@link Payment} instances without scattering {@code toBuilder()} usage.
 */
public final class PaymentTestFixture {

    private PaymentTestFixture() {
    }

    public static Payment copy(Payment originPayment, Consumer<Payment.PaymentBuilder> builderCustomizer) {
        Payment.PaymentBuilder paymentBuilder = originPayment.toBuilder();
        if (builderCustomizer != null) {
            builderCustomizer.accept(paymentBuilder);
        }
        return paymentBuilder.build();
    }
}
