package com.example.shop.order.presentation.controller;

import com.example.shop.common.infrastructure.config.security.auth.CustomUserDetails;
import com.example.shop.common.presentation.dto.ApiDto;
import com.example.shop.order.application.service.OrderServiceV1;
import com.example.shop.order.presentation.dto.request.ReqPostOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResGetOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResGetOrderDtoV1;
import com.example.shop.order.presentation.dto.response.ResPostOrdersDtoV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1/orders")
public class OrderControllerV1 {

    private final OrderServiceV1 orderServiceV1;

    @GetMapping
    public ResponseEntity<ApiDto<ResGetOrdersDtoV1>> getOrders(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        ResGetOrdersDtoV1 responseBody = orderServiceV1.getOrders(
                customUserDetails.getUser().getId(),
                customUserDetails.getUser().getRoleList(),
                pageable
        );
        return ResponseEntity.ok(
                ApiDto.<ResGetOrdersDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiDto<ResGetOrderDtoV1>> getOrder(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable("id") UUID orderId
    ) {
        ResGetOrderDtoV1 responseBody = orderServiceV1.getOrder(
                customUserDetails.getUser().getId(),
                customUserDetails.getUser().getRoleList(),
                orderId
        );
        return ResponseEntity.ok(
                ApiDto.<ResGetOrderDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiDto<ResPostOrdersDtoV1>> postOrders(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody @Valid ReqPostOrdersDtoV1 reqDto
    ) {
        ResPostOrdersDtoV1 responseBody = orderServiceV1.postOrders(customUserDetails.getUser().getId(), reqDto);
        return ResponseEntity.ok(
                ApiDto.<ResPostOrdersDtoV1>builder()
                        .message("주문이 생성되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiDto<Object>> cancelOrder(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable("id") UUID orderId
    ) {
        orderServiceV1.cancelOrder(
                customUserDetails.getUser().getId(),
                customUserDetails.getUser().getRoleList(),
                orderId
        );
        return ResponseEntity.ok(
                ApiDto.builder()
                        .message("주문이 취소되었습니다.")
                        .build()
        );
    }
}
