package com.example.shopmark2.order.infrastructure.persistence.mapper;

import com.example.shopmark2.order.domain.model.Order;
import com.example.shopmark2.order.domain.model.OrderItem;
import com.example.shopmark2.order.infrastructure.persistence.entity.OrderEntity;
import com.example.shopmark2.order.infrastructure.persistence.entity.OrderItemEntity;
import com.example.shopmark2.payment.domain.model.Payment;
import com.example.shopmark2.payment.infrastructure.persistence.mapper.PaymentMapper;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    private final PaymentMapper paymentMapper;

    public OrderMapper(PaymentMapper paymentMapper) {
        this.paymentMapper = paymentMapper;
    }

    public Order toDomain(OrderEntity orderEntity) {
        if (orderEntity == null) {
            return null;
        }

        Payment payment = Optional.ofNullable(orderEntity.getPayment())
                .map(paymentMapper::toDomain)
                .orElse(null);

        return Order.builder()
                .id(orderEntity.getId())
                .userId(orderEntity.getUser() != null ? orderEntity.getUser().getId() : null)
                .status(toDomain(orderEntity.getStatus()))
                .totalAmount(orderEntity.getTotalAmount())
                .orderItemList(orderEntity.getOrderItemList()
                        .stream()
                        .map(this::toDomain)
                        .toList())
                .payment(payment)
                .createdAt(orderEntity.getCreatedAt())
                .createdBy(orderEntity.getCreatedBy())
                .updatedAt(orderEntity.getUpdatedAt())
                .updatedBy(orderEntity.getUpdatedBy())
                .deletedAt(orderEntity.getDeletedAt())
                .deletedBy(orderEntity.getDeletedBy())
                .build();
    }

    private OrderItem toDomain(OrderItemEntity orderItemEntity) {
        if (orderItemEntity == null) {
            return null;
        }
        return OrderItem.builder()
                .id(orderItemEntity.getId())
                .orderId(orderItemEntity.getOrder() != null ? orderItemEntity.getOrder().getId() : null)
                .productId(orderItemEntity.getProductId())
                .productName(orderItemEntity.getProductName())
                .unitPrice(orderItemEntity.getUnitPrice())
                .quantity(orderItemEntity.getQuantity())
                .lineTotal(orderItemEntity.getLineTotal())
                .createdAt(orderItemEntity.getCreatedAt())
                .createdBy(orderItemEntity.getCreatedBy())
                .updatedAt(orderItemEntity.getUpdatedAt())
                .updatedBy(orderItemEntity.getUpdatedBy())
                .deletedAt(orderItemEntity.getDeletedAt())
                .deletedBy(orderItemEntity.getDeletedBy())
                .build();
    }

    public OrderEntity toEntity(Order order) {
        if (order == null) {
            return null;
        }
        OrderEntity orderEntity = OrderEntity.builder()
                .id(order.getId())
                .status(toEntity(order.getStatus()))
                .totalAmount(order.getTotalAmount())
                .build();

        order.getOrderItemList()
                .stream()
                .map(this::toEntity)
                .forEach(orderEntity::addOrderItem);

        return orderEntity;
    }

    public void applyDomain(Order order, OrderEntity orderEntity) {
        if (order == null || orderEntity == null) {
            return;
        }
        orderEntity.updateStatus(toEntity(order.getStatus()));
        orderEntity.updateTotalAmount(order.getTotalAmount());
        syncOrderItems(order, orderEntity);
    }

    private void syncOrderItems(Order order, OrderEntity orderEntity) {
        Map<UUID, OrderItemEntity> currentItemMap = orderEntity.getOrderItemList()
                .stream()
                .filter(orderItem -> orderItem.getId() != null)
                .collect(Collectors.toMap(OrderItemEntity::getId, Function.identity()));

        Set<UUID> desiredIds = new HashSet<>();
        order.getOrderItemList().forEach(orderItem -> {
            UUID orderItemId = orderItem.getId();
            if (orderItemId != null && currentItemMap.containsKey(orderItemId)) {
                OrderItemEntity existingOrderItem = currentItemMap.get(orderItemId);
                existingOrderItem.update(
                        orderItem.getProductId(),
                        orderItem.getProductName(),
                        orderItem.getUnitPrice(),
                        orderItem.getQuantity(),
                        orderItem.getLineTotal()
                );
                desiredIds.add(orderItemId);
            } else {
                OrderItemEntity orderItemEntity = toEntity(orderItem);
                orderEntity.addOrderItem(orderItemEntity);
                if (orderItemEntity.getId() != null) {
                    desiredIds.add(orderItemEntity.getId());
                }
            }
        });

        List<OrderItemEntity> itemsToRemove = orderEntity.getOrderItemList()
                .stream()
                .filter(orderItemEntity -> orderItemEntity.getId() != null && !desiredIds.contains(orderItemEntity.getId()))
                .toList();
        itemsToRemove.forEach(orderItemEntity -> {
            orderItemEntity.detachOrder();
            orderEntity.getOrderItemList().remove(orderItemEntity);
        });
    }

    private OrderItemEntity toEntity(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        return OrderItemEntity.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductName())
                .unitPrice(orderItem.getUnitPrice())
                .quantity(orderItem.getQuantity())
                .lineTotal(orderItem.getLineTotal())
                .build();
    }

    private Order.Status toDomain(OrderEntity.Status status) {
        if (status == null) {
            return null;
        }
        return Order.Status.valueOf(status.name());
    }

    private OrderEntity.Status toEntity(Order.Status status) {
        if (status == null) {
            return null;
        }
        return OrderEntity.Status.valueOf(status.name());
    }
}
