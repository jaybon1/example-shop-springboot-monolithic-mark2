package com.example.shop.product.presentation.dto.response;

import com.example.shop.product.domain.model.Product;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResGetProductDtoV1 {

    private ProductDto product;

    public static ResGetProductDtoV1 of(Product product) {
        return ResGetProductDtoV1.builder()
                .product(ProductDto.from(product))
                .build();
    }

    @Getter
    @Builder
    public static class ProductDto {

        private String id;

        private String name;

        private Long price;

        private Long stock;

        public static ProductDto from(Product product) {
            return ProductDto.builder()
                    .id(product.getId().toString())
                    .name(product.getName())
                    .price(product.getPrice())
                    .stock(product.getStock())
                    .build();
        }

    }

}
