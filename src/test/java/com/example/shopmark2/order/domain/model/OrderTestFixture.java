package com.example.shopmark2.order.domain.model;

import java.util.function.Consumer;

/**
 * Test-only helper to clone {@link Order} instances while keeping {@code toBuilder()} usage in one place.
 */
public final class OrderTestFixture {

    private OrderTestFixture() {
    }

    public static Order copy(Order originOrder, Consumer<Order.OrderBuilder> builderCustomizer) {
        Order.OrderBuilder orderBuilder = originOrder.toBuilder();
        if (builderCustomizer != null) {
            builderCustomizer.accept(orderBuilder);
        }
        return orderBuilder.build();
    }
}
