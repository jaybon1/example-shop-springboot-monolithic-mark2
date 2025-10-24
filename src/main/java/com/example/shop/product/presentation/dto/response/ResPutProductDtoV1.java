package com.example.shop.product.presentation.dto.response;

import com.example.shop.product.domain.model.Product;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPutProductDtoV1 {

    private ProductDto product;

    public static ResPutProductDtoV1 of(Product product) {
        return ResPutProductDtoV1.builder()
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
