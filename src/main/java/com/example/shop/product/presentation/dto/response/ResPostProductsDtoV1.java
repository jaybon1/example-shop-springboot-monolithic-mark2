package com.example.shop.product.presentation.dto.response;

import com.example.shop.product.domain.model.Product;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPostProductsDtoV1 {

    private ProductDto product;

    public static ResPostProductsDtoV1 of(Product product) {
        return ResPostProductsDtoV1.builder()
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
