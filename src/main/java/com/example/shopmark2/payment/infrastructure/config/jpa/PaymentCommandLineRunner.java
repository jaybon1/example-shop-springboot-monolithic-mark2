package com.example.shopmark2.payment.infrastructure.config.jpa;

import com.example.shopmark2.order.domain.model.Order;
import com.example.shopmark2.order.domain.repository.OrderRepository;
import com.example.shopmark2.payment.domain.model.Payment;
import com.example.shopmark2.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class PaymentCommandLineRunner implements CommandLineRunner {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    @Override
    public void run(String... args) {
        if (paymentRepository.count() > 0) {
            return;
        }

        List<Order> orderList = orderRepository.findAll(Pageable.unpaged()).getContent();
        if (orderList.isEmpty()) {
            return;
        }

        for (int index = 0; index < orderList.size(); index++) {
            Order order = orderList.get(index);
            createCompletedPayment(order, index);
        }
    }

    private void createCompletedPayment(Order order, int seedIndex) {
        if (Order.Status.CANCELLED.equals(order.getStatus())) {
            return;
        }

        if (order.getPayment() != null) {
            return;
        }

        if (order.getUserId() == null) {
            return;
        }

        Long totalAmount = Optional.ofNullable(order.getTotalAmount()).orElse(0L);
        Payment payment = Payment.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .method(Payment.Method.CARD)
                .amount(totalAmount)
                .transactionKey("demo-transaction-%d".formatted(seedIndex + 1))
                .status(Payment.Status.COMPLETED)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        Order updatedOrder = order.assignPayment(savedPayment).markPaid();
        orderRepository.save(updatedOrder);
    }
}
