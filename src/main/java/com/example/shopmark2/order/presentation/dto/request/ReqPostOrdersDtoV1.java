package com.example.shopmark2.order.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ReqPostOrdersDtoV1 {

    @NotNull(message = "주문 정보를 입력해주세요.")
    @Valid
    private OrderDto order;

    @Getter
    @Builder
    public static class OrderDto {

        @NotNull(message = "주문 상품을 입력해주세요.")
        @Size(min = 1, message = "주문 상품을 1개 이상 입력해주세요.")
        @Valid
        private List<OrderItemDto> orderItemList;

        @Getter
        @Builder
        public static class OrderItemDto {

            @NotNull(message = "상품 ID를 입력해주세요.")
            private UUID productId;

            @NotNull(message = "수량을 입력해주세요.")
            @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
            private Long quantity;
        }
    }

}
