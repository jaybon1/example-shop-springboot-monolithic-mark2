package com.example.shop.product.domain.model;

import java.util.function.Consumer;

/**
 * Test-only helper to clone {@link Product} instances safely without exposing {@code toBuilder()} to every test.
 */
public final class ProductTestFixture {

    private ProductTestFixture() {
    }

    public static Product copy(Product originProduct, Consumer<Product.ProductBuilder> builderCustomizer) {
        Product.ProductBuilder productBuilder = originProduct.toBuilder();
        if (builderCustomizer != null) {
            builderCustomizer.accept(productBuilder);
        }
        return productBuilder.build();
    }
}
