package com.example.shop.payment.presentation.controller;

import com.example.shop.common.infrastructure.config.security.auth.CustomUserDetails;
import com.example.shop.common.presentation.dto.ApiDto;
import com.example.shop.payment.application.service.PaymentServiceV1;
import com.example.shop.payment.presentation.dto.request.ReqPostPaymentsDtoV1;
import com.example.shop.payment.presentation.dto.response.ResGetPaymentDtoV1;
import com.example.shop.payment.presentation.dto.response.ResPostPaymentsDtoV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1/payments")
public class PaymentControllerV1 {

    private final PaymentServiceV1 paymentServiceV1;

    @GetMapping("/{id}")
    public ResponseEntity<ApiDto<ResGetPaymentDtoV1>> getPayment(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable("id") UUID paymentId
    ) {
        ResGetPaymentDtoV1 responseBody = paymentServiceV1.getPayment(
                paymentId,
                customUserDetails.getUser().getId(),
                customUserDetails.getUser().getRoleList()
        );
        return ResponseEntity.ok(
                ApiDto.<ResGetPaymentDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiDto<ResPostPaymentsDtoV1>> postPayments(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody @Valid ReqPostPaymentsDtoV1 reqDto
    ) {
        ResPostPaymentsDtoV1 responseBody = paymentServiceV1.postPayments(customUserDetails.getUser().getId(), reqDto);
        return ResponseEntity.ok(
                ApiDto.<ResPostPaymentsDtoV1>builder()
                        .message("결제가 완료되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }

}
