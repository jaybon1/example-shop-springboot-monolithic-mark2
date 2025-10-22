package com.example.shopmark2.order.domain.model;

import java.util.function.Consumer;

/**
 * Test-only helper to clone {@link OrderItem} instances while centralising {@code toBuilder()} usage.
 */
public final class OrderItemTestFixture {

    private OrderItemTestFixture() {
    }

    public static OrderItem copy(OrderItem originOrderItem, Consumer<OrderItem.OrderItemBuilder> builderCustomizer) {
        OrderItem.OrderItemBuilder orderItemBuilder = originOrderItem.toBuilder();
        if (builderCustomizer != null) {
            builderCustomizer.accept(orderItemBuilder);
        }
        return orderItemBuilder.build();
    }
}
