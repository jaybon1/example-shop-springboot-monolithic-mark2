package com.example.shop.product.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReqPutProductDtoV1 {

    @NotNull(message = "상품 정보를 입력해주세요.")
    @Valid
    private ProductDto product;

    @Getter
    @Builder
    public static class ProductDto {

        private String name;

        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        private Long price;

        @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
        private Long stock;

    }

}
