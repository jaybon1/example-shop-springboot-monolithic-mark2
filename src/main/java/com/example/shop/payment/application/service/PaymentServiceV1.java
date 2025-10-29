package com.example.shop.payment.application.service;

import com.example.shop.order.domain.model.Order;
import com.example.shop.order.domain.repository.OrderRepository;
import com.example.shop.payment.domain.model.Payment;
import com.example.shop.payment.domain.repository.PaymentRepository;
import com.example.shop.payment.presentation.advice.PaymentError;
import com.example.shop.payment.presentation.advice.PaymentException;
import com.example.shop.payment.presentation.dto.request.ReqPostPaymentsDtoV1;
import com.example.shop.payment.presentation.dto.response.ResGetPaymentDtoV1;
import com.example.shop.payment.presentation.dto.response.ResPostPaymentsDtoV1;
import com.example.shop.user.domain.model.User;
import com.example.shop.user.domain.model.UserRole;
import com.example.shop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceV1 {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ResGetPaymentDtoV1 getPayment(UUID paymentId, UUID authUserId, List<String> authUserRoleList) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentError.PAYMENT_NOT_FOUND));

        if (!payment.isOwnedBy(authUserId) && !isAdminOrManager(authUserRoleList)) {
            throw new PaymentException(PaymentError.PAYMENT_FORBIDDEN);
        }

        Order order = payment.getOrderId() != null
                ? orderRepository.findById(payment.getOrderId()).orElse(null)
                : null;

        User user = payment.getUserId() != null
                ? userRepository.findById(payment.getUserId()).orElse(null)
                : null;

        return ResGetPaymentDtoV1.of(payment, order, user);
    }

    @Transactional
    public ResPostPaymentsDtoV1 postPayments(UUID authUserId, ReqPostPaymentsDtoV1 reqDto) {
        ReqPostPaymentsDtoV1.PaymentDto reqPayment = reqDto.getPayment();
        UUID orderId = reqPayment.getOrderId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new PaymentException(PaymentError.PAYMENT_ORDER_NOT_FOUND));

        if (!order.isOwnedBy(authUserId)) {
            throw new PaymentException(PaymentError.PAYMENT_ORDER_FORBIDDEN);
        }

        if (Order.Status.CANCELLED.equals(order.getStatus())) {
            throw new PaymentException(PaymentError.PAYMENT_ORDER_CANCELLED);
        }

        if (Order.Status.PAID.equals(order.getStatus())) {
            throw new PaymentException(PaymentError.PAYMENT_ALREADY_EXISTS);
        }

        if (order.getPayment() != null) {
            throw new PaymentException(PaymentError.PAYMENT_ALREADY_EXISTS);
        }

        User paymentUser = findUser(authUserId);

        Payment payment = Payment.builder()
                .orderId(order.getId())
                .userId(paymentUser.getId())
                .method(reqPayment.getMethod())
                .amount(order.getTotalAmount())
                .transactionKey(reqPayment.getTransactionKey())
                .status(Payment.Status.COMPLETED)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        Order updatedOrder = order.assignPayment(savedPayment).markPaid();
        Order savedOrder = orderRepository.save(updatedOrder);

        return ResPostPaymentsDtoV1.of(savedPayment, savedOrder);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new PaymentException(PaymentError.PAYMENT_USER_NOT_FOUND));
    }

    private boolean isAdminOrManager(List<String> authUserRoleList) {
        if (authUserRoleList == null) {
            return false;
        }

        return authUserRoleList.contains(UserRole.Role.ADMIN.toString())
                || authUserRoleList.contains(UserRole.Role.MANAGER.toString());
    }
}
