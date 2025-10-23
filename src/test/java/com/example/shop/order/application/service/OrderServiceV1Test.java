package com.example.shop.order.application.service;

import com.example.shop.order.domain.model.Order;
import com.example.shop.order.domain.model.OrderItem;
import com.example.shop.order.domain.model.OrderItemTestFixture;
import com.example.shop.order.domain.model.OrderTestFixture;
import com.example.shop.order.domain.repository.OrderRepository;
import com.example.shop.order.presentation.advice.OrderError;
import com.example.shop.order.presentation.advice.OrderException;
import com.example.shop.order.presentation.dto.request.ReqPostOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResPostOrdersDtoV1;
import com.example.shop.payment.domain.model.Payment;
import com.example.shop.payment.domain.repository.PaymentRepository;
import com.example.shop.payment.presentation.advice.PaymentError;
import com.example.shop.payment.presentation.advice.PaymentException;
import com.example.shop.product.domain.model.Product;
import com.example.shop.product.domain.repository.ProductRepository;
import com.example.shop.user.domain.model.User;
import com.example.shop.user.domain.model.UserRole;
import com.example.shop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceV1Test {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private OrderServiceV1 orderServiceV1;

    private User user;
    private Product product;
    private Order existingOrder;

    @BeforeEach
    void setUp() {
        user = createUser(UUID.randomUUID(), "order-user", UserRole.Role.USER);
        product = Product.builder()
                .id(UUID.randomUUID())
                .name("sample-product")
                .price(1000L)
                .stock(10L)
                .build();
        existingOrder = Order.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .status(Order.Status.CREATED)
                .orderItemList(List.of(
                        OrderItem.builder()
                                .id(UUID.randomUUID())
                                .orderId(UUID.randomUUID())
                                .productId(product.getId())
                                .productName(product.getName())
                                .unitPrice(product.getPrice())
                                .quantity(1L)
                                .lineTotal(product.getPrice())
                                .build()
                ))
                .build();
    }

    @Test
    @DisplayName("사용자는 자신의 주문 목록만 페이지로 조회할 수 있다")
    void getOrdersForSelf() {
        Pageable pageable = PageRequest.of(0, 5);
        when(orderRepository.findByUserId(user.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(existingOrder)));

        var response = orderServiceV1.getOrders(user.getId(), List.of(UserRole.Role.USER.toString()), pageable);

        assertThat(response.getOrderPage().getContent()).hasSize(1);
        verify(orderRepository).findByUserId(user.getId(), pageable);
    }

    @Test
    @DisplayName("관리자는 모든 주문 목록을 조회할 수 있다")
    void getOrdersAsAdmin() {
        Pageable pageable = PageRequest.of(0, 5);
        when(orderRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(existingOrder)));

        var response = orderServiceV1.getOrders(UUID.randomUUID(), List.of(UserRole.Role.ADMIN.toString()), pageable);

        assertThat(response.getOrderPage().getContent()).hasSize(1);
        verify(orderRepository).findAll(pageable);
    }

    @Test
    @DisplayName("주문 생성 시 상품 재고 차감 및 총액 계산이 수행된다")
    void postOrdersUpdatesStockAndTotalAmount() {
        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            UUID persistedOrderId = UUID.randomUUID();
            return OrderTestFixture.copy(order, orderBuilder -> orderBuilder
                    .id(persistedOrderId)
                    .orderItemList(order.getOrderItemList()
                            .stream()
                            .map(item -> OrderItemTestFixture.copy(item, itemBuilder -> itemBuilder
                                    .id(UUID.randomUUID())
                                    .orderId(persistedOrderId)))
                            .toList()));
        });
        when(userRepository.findDefaultById(user.getId())).thenReturn(user);

        ReqPostOrdersDtoV1 reqDto = ReqPostOrdersDtoV1.builder()
                .order(ReqPostOrdersDtoV1.OrderDto.builder()
                        .orderItemList(List.of(
                                ReqPostOrdersDtoV1.OrderDto.OrderItemDto.builder()
                                        .productId(product.getId())
                                        .quantity(2L)
                                        .build()
                        ))
                        .build())
                .build();

        ResPostOrdersDtoV1 response = orderServiceV1.postOrders(user.getId(), reqDto);

        assertThat(response.getOrder().getTotalAmount()).isEqualTo(2000L);
        verify(productRepository).save(argThat(savedProduct ->
                savedProduct.getId().equals(product.getId()) && savedProduct.getStock().equals(8L)
        ));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 취소는 사용자 본인 또는 관리자/매니저만 가능하다")
    void cancelOrdersWithAuthority() {
        Order paidOrder = existingOrder
                .assignPayment(createPayment(existingOrder.getId(), user.getId()))
                .markPaid();
        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(paidOrder));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderServiceV1.cancelOrdersWithId(user.getId(), List.of(UserRole.Role.USER.toString()), existingOrder.getId());

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, atLeastOnce()).save(captor.capture());
        Order savedOrder = captor.getValue();
        assertThat(savedOrder.getStatus()).isEqualTo(Order.Status.CANCELLED);
    }

    @Test
    @DisplayName("주문 취소 시 권한이 없으면 예외를 던진다")
    void cancelOrdersForbidden() {
        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(existingOrder));

        assertThatThrownBy(() -> orderServiceV1.cancelOrdersWithId(UUID.randomUUID(), List.of("USER"), existingOrder.getId()))
                .isInstanceOf(OrderException.class)
                .extracting(Throwable::getMessage)
                .asString()
                .contains(OrderError.ORDER_FORBIDDEN.getErrorMessage());
    }

    @Test
    @DisplayName("환불 시 결제를 찾지 못하면 예외를 던진다")
    void cancelOrdersPaymentMissing() {
        Order paidOrderWithoutPayment = existingOrder.markPaid();
        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(paidOrderWithoutPayment));

        assertThatThrownBy(() -> orderServiceV1.cancelOrdersWithId(user.getId(), List.of(UserRole.Role.USER.toString()), existingOrder.getId()))
                .isInstanceOf(PaymentException.class)
                .extracting(Throwable::getMessage)
                .asString()
                .contains(PaymentError.PAYMENT_NOT_FOUND.getErrorMessage());
    }

    private User createUser(UUID id, String username, UserRole.Role role) {
        return User.builder()
                .id(id)
                .username(username)
                .password("secure-password")
                .nickname("nick-" + username)
                .email(username + "@example.com")
                .jwtValidator(0L)
                .userRoleList(List.of(UserRole.builder().id(UUID.randomUUID()).role(role).build()))
                .userSocialList(List.of())
                .build();
    }

    private Payment createPayment(UUID orderId, UUID userId) {
        return Payment.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .userId(userId)
                .status(Payment.Status.COMPLETED)
                .method(Payment.Method.CARD)
                .amount(1500L)
                .transactionKey("tx-001")
                .build();
    }
}
