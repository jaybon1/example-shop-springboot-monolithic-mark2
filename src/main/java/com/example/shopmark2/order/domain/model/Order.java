package com.example.shopmark2.order.domain.model;

import com.example.shopmark2.payment.domain.model.Payment;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@EqualsAndHashCode(of = "id")
public class Order {

    private final UUID id;
    private final UUID userId;
    private final Status status;
    private final Long totalAmount;
    private final List<OrderItem> orderItemList;
    private final Payment payment;
    private final Instant createdAt;
    private final String createdBy;
    private final Instant updatedAt;
    private final String updatedBy;
    private final Instant deletedAt;
    private final String deletedBy;

    public List<OrderItem> getOrderItemList() {
        return orderItemList == null ? List.of() : Collections.unmodifiableList(orderItemList);
    }

    public Order addOrderItem(OrderItem orderItem) {
        if (orderItem == null) {
            return this;
        }
        List<OrderItem> updatedOrderItemList = new ArrayList<>(getOrderItemList());
        updatedOrderItemList.add(orderItem);
        return this.toBuilder()
                .orderItemList(List.copyOf(updatedOrderItemList))
                .build();
    }

    public Order updateTotalAmount(Long totalAmount) {
        return this.toBuilder()
                .totalAmount(totalAmount)
                .build();
    }

    public Order markPaid() {
        return this.toBuilder()
                .status(Status.PAID)
                .build();
    }

    public Order markCancelled() {
        return this.toBuilder()
                .status(Status.CANCELLED)
                .build();
    }

    public Order assignPayment(Payment payment) {
        return this.toBuilder()
                .payment(payment)
                .build();
    }

    OrderBuilder toBuilder() {
        return Order.builder()
                .id(id)
                .userId(userId)
                .status(status)
                .totalAmount(totalAmount)
                .orderItemList(orderItemList)
                .payment(payment)
                .createdAt(createdAt)
                .createdBy(createdBy)
                .updatedAt(updatedAt)
                .updatedBy(updatedBy)
                .deletedAt(deletedAt)
                .deletedBy(deletedBy);
    }

    public boolean isOwnedBy(UUID userId) {
        return userId != null && userId.equals(this.userId);
    }

    public enum Status {
        CREATED,
        PAID,
        CANCELLED
    }
}
