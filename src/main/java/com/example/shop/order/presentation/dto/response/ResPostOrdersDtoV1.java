package com.example.shop.order.presentation.dto.response;

import com.example.shop.order.domain.model.Order;
import com.example.shop.order.domain.model.OrderItem;
import com.example.shop.payment.domain.model.Payment;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ResPostOrdersDtoV1 {

    private OrderDto order;

    public static ResPostOrdersDtoV1 of(Order order, Payment payment) {
        return ResPostOrdersDtoV1.builder()
                .order(OrderDto.from(order, payment != null ? payment : order.getPayment()))
                .build();
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
        private PaymentDto payment;

        public static OrderDto from(Order order, Payment payment) {
            return OrderDto.builder()
                    .id(order.getId() != null ? order.getId().toString() : null)
                    .status(order.getStatus())
                    .totalAmount(order.getTotalAmount())
                    .createdAt(order.getCreatedAt())
                    .updatedAt(order.getUpdatedAt())
                    .orderItemList(OrderItemDto.from(order.getOrderItemList()))
                    .payment(PaymentDto.from(payment))
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

        private static List<OrderItemDto> from(List<OrderItem> orderItemList) {
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
    public static class PaymentDto {

        private String id;
        private Payment.Status status;
        private Payment.Method method;
        private Long amount;
        private String transactionKey;

        public static PaymentDto from(Payment payment) {
            if (payment == null) {
                return null;
            }
            return PaymentDto.builder()
                    .id(payment.getId() != null ? payment.getId().toString() : null)
                    .status(payment.getStatus())
                    .method(payment.getMethod())
                    .amount(payment.getAmount())
                    .transactionKey(payment.getTransactionKey())
                    .build();
        }
    }
}
