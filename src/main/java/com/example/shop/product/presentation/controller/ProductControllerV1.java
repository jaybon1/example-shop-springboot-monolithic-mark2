package com.example.shop.product.presentation.controller;

import com.example.shop.common.infrastructure.config.security.auth.CustomUserDetails;
import com.example.shop.common.presentation.dto.ApiDto;
import com.example.shop.product.application.service.ProductServiceV1;
import com.example.shop.product.presentation.dto.request.ReqPostProductsDtoV1;
import com.example.shop.product.presentation.dto.request.ReqPutProductsWithIdDtoV1;
import com.example.shop.product.presentation.dto.response.ResGetProductsDtoV1;
import com.example.shop.product.presentation.dto.response.ResGetProductsWithIdDtoV1;
import com.example.shop.product.presentation.dto.response.ResPostProductsDtoV1;
import com.example.shop.product.presentation.dto.response.ResPutProductsWithIdDtoV1;
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
@RequestMapping("/v1/products")
public class ProductControllerV1 {

    private final ProductServiceV1 productServiceV1;

    @GetMapping
    public ResponseEntity<ApiDto<ResGetProductsDtoV1>> getProducts(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "name", required = false) String name
    ) {
        ResGetProductsDtoV1 responseBody = productServiceV1.getProducts(pageable, name);

        return ResponseEntity.ok(
                ApiDto.<ResGetProductsDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiDto<ResGetProductsWithIdDtoV1>> getProductsWithId(
            @PathVariable("id") UUID productId
    ) {
        ResGetProductsWithIdDtoV1 responseBody = productServiceV1.getProductsWithId(productId);

        return ResponseEntity.ok(
                ApiDto.<ResGetProductsWithIdDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiDto<ResPostProductsDtoV1>> postProducts(
            @RequestBody @Valid ReqPostProductsDtoV1 reqDto
    ) {
        ResPostProductsDtoV1 responseBody = productServiceV1.postProducts(reqDto);
        return ResponseEntity.ok(
                ApiDto.<ResPostProductsDtoV1>builder()
                        .message("상품 등록이 완료되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiDto<ResPutProductsWithIdDtoV1>> putProductsWithId(
            @PathVariable("id") UUID productId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody @Valid ReqPutProductsWithIdDtoV1 reqDto
    ) {
        ResPutProductsWithIdDtoV1 responseBody = productServiceV1.putProductsWithId(
                customUserDetails.getUser().getId(),
                customUserDetails.getUser().getRoleList(),
                productId,
                reqDto
        );
        return ResponseEntity.ok(
                ApiDto.<ResPutProductsWithIdDtoV1>builder()
                        .message("상품 정보가 업데이트 되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiDto<Object>> deleteProductsWithId(
            @PathVariable("id") UUID productId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        productServiceV1.deleteProductsWithId(
                customUserDetails.getUser().getId(),
                customUserDetails.getUser().getRoleList(),
                productId
        );
        return ResponseEntity.ok(
                ApiDto.builder()
                        .message("상품 삭제가 완료되었습니다.")
                        .build()
        );
    }

}
