package com.example.shop.payment.application.service;

import com.example.shop.order.domain.model.Order;
import com.example.shop.order.domain.model.OrderItem;
import com.example.shop.order.domain.repository.OrderRepository;
import com.example.shop.payment.domain.model.Payment;
import com.example.shop.payment.domain.model.PaymentTestFixture;
import com.example.shop.payment.domain.repository.PaymentRepository;
import com.example.shop.payment.presentation.advice.PaymentError;
import com.example.shop.payment.presentation.advice.PaymentException;
import com.example.shop.payment.presentation.dto.request.ReqPostPaymentsDtoV1;
import com.example.shop.payment.presentation.dto.response.ResPostPaymentsDtoV1;
import com.example.shop.user.domain.model.User;
import com.example.shop.user.domain.model.UserRole;
import com.example.shop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceV1Test {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentServiceV1 paymentServiceV1;

    private User user;
    private Order order;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("pay-user")
                .password("pay-pass")
                .nickname("pay-nick")
                .email("pay@example.com")
                .jwtValidator(0L)
                .userRoleList(List.of(UserRole.builder().id(UUID.randomUUID()).role(UserRole.Role.USER).build()))
                .userSocialList(List.of())
                .build();

        order = Order.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .status(Order.Status.CREATED)
                .totalAmount(3000L)
                .orderItemList(List.of(
                        OrderItem.builder()
                                .id(UUID.randomUUID())
                                .orderId(null)
                                .productId(UUID.randomUUID())
                                .productName("sample")
                                .unitPrice(3000L)
                                .quantity(1L)
                                .lineTotal(3000L)
                                .build()
                ))
                .build();
    }

    @Test
    @DisplayName("결제를 생성하면 주문 상태가 PAID로 변경된다")
    void postPaymentsMarksOrderPaid() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            return PaymentTestFixture.copy(payment, builder -> builder.id(UUID.randomUUID()));
        });
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReqPostPaymentsDtoV1 reqDto = ReqPostPaymentsDtoV1.builder()
                .payment(ReqPostPaymentsDtoV1.PaymentDto.builder()
                        .orderId(order.getId())
                        .method(Payment.Method.CARD)
                        .transactionKey("tx-key")
                        .build())
                .build();

        ResPostPaymentsDtoV1 response = paymentServiceV1.postPayments(user.getId(), reqDto);

        assertThat(response.getPayment().getOrderStatus()).isEqualTo(Order.Status.PAID);
        verify(paymentRepository).save(any(Payment.class));
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
    }

    @Test
    @DisplayName("다른 사용자의 주문을 결제하려면 예외가 발생한다")
    void postPaymentsForbiddenForDifferentUser() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        ReqPostPaymentsDtoV1 reqDto = ReqPostPaymentsDtoV1.builder()
                .payment(ReqPostPaymentsDtoV1.PaymentDto.builder()
                        .orderId(order.getId())
                        .method(Payment.Method.CARD)
                        .transactionKey("tx-key")
                        .build())
                .build();

        assertThatThrownBy(() -> paymentServiceV1.postPayments(UUID.randomUUID(), reqDto))
                .isInstanceOf(PaymentException.class)
                .extracting(Throwable::getMessage)
                .asString()
                .contains(PaymentError.PAYMENT_ORDER_FORBIDDEN.getErrorMessage());
    }
}
