package com.example.shopmark2.product.presentation.dto.response;

import com.example.shopmark2.product.domain.model.Product;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPutProductsWithIdDtoV1 {

    private ProductDto product;

    public static ResPutProductsWithIdDtoV1 of(Product product) {
        return ResPutProductsWithIdDtoV1.builder()
                .product(ProductDto.from(product))
                .build();
    }

    @Getter
    @Builder
    public static class ProductDto {

        private String id;

        public static ProductDto from(Product product) {
            return ProductDto.builder()
                    .id(product.getId().toString())
                    .build();
        }

    }

}
