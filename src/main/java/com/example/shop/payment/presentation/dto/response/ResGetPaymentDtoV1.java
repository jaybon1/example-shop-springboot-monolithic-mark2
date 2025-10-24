package com.example.shop.payment.presentation.dto.response;

import com.example.shop.order.domain.model.Order;
import com.example.shop.order.domain.model.OrderItem;
import com.example.shop.payment.domain.model.Payment;
import com.example.shop.user.domain.model.User;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ResGetPaymentDtoV1 {

    private PaymentDto payment;

    public static ResGetPaymentDtoV1 of(Payment payment, Order order, User user) {
        return ResGetPaymentDtoV1.builder()
                .payment(PaymentDto.from(payment, order, user))
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
        private Instant createdAt;
        private Instant updatedAt;
        private OrderDto order;
        private UserDto user;

        public static PaymentDto from(Payment payment, Order order, User user) {
            return PaymentDto.builder()
                    .id(payment.getId() != null ? payment.getId().toString() : null)
                    .status(payment.getStatus())
                    .method(payment.getMethod())
                    .amount(payment.getAmount())
                    .transactionKey(payment.getTransactionKey())
                    .createdAt(payment.getCreatedAt())
                    .updatedAt(payment.getUpdatedAt())
                    .order(OrderDto.from(order, payment.getOrderId(), payment.getAmount()))
                    .user(UserDto.from(user, payment.getUserId()))
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OrderDto {

        private String id;
        private Order.Status status;
        private Long totalAmount;
        private Instant createdAt;
        private Instant updatedAt;
        private List<OrderItemDto> orderItemList;

        public static OrderDto from(Order order, UUID orderId, Long paymentAmount) {
            if (order != null) {
                return OrderDto.builder()
                        .id(order.getId() != null ? order.getId().toString() : null)
                        .status(order.getStatus())
                        .totalAmount(order.getTotalAmount())
                        .createdAt(order.getCreatedAt())
                        .updatedAt(order.getUpdatedAt())
                        .orderItemList(OrderItemDto.from(order.getOrderItemList()))
                        .build();
            }
            return OrderDto.builder()
                    .id(orderId != null ? orderId.toString() : null)
                    .status(null)
                    .totalAmount(paymentAmount)
                    .createdAt(null)
                    .updatedAt(null)
                    .orderItemList(List.of())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OrderItemDto {

        private String id;
        private String productId;
        private String productName;
        private Long unitPrice;
        private Long quantity;
        private Long lineTotal;

        public static List<OrderItemDto> from(List<OrderItem> orderItemList) {
            return orderItemList.stream()
                    .map(OrderItemDto::from)
                    .toList();
        }

        public static OrderItemDto from(OrderItem orderItem) {
            if (orderItem == null) {
                return null;
            }
            return OrderItemDto.builder()
                    .id(orderItem.getId() != null ? orderItem.getId().toString() : null)
                    .productId(orderItem.getProductId() != null ? orderItem.getProductId().toString() : null)
                    .productName(orderItem.getProductName())
                    .unitPrice(orderItem.getUnitPrice())
                    .quantity(orderItem.getQuantity())
                    .lineTotal(orderItem.getLineTotal())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class UserDto {

        private String id;
        private String username;
        private String nickname;
        private String email;

        public static UserDto from(User user, UUID userId) {
            if (user == null) {
                return UserDto.builder()
                        .id(userId != null ? userId.toString() : null)
                        .build();
            }
            return UserDto.builder()
                    .id(user.getId() != null ? user.getId().toString() : null)
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .build();
        }
    }
}
