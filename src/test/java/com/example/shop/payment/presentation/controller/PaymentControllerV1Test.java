package com.example.shop.payment.presentation.controller;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.order.domain.model.Order;
import com.example.shop.order.presentation.dto.request.ReqPostOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResPostOrdersDtoV1;
import com.example.shop.payment.domain.model.Payment.Method;
import com.example.shop.payment.domain.model.Payment.Status;
import com.example.shop.payment.presentation.dto.request.ReqPostPaymentsDtoV1;
import com.example.shop.payment.presentation.dto.response.ResGetPaymentsWithIdDtoV1;
import com.example.shop.payment.presentation.dto.response.ResPostPaymentsDtoV1;
import com.example.shop.product.domain.model.Product;
import com.example.shop.product.domain.repository.ProductRepository;
import com.example.shop.user.presentation.dto.request.ReqPostAuthLoginDtoV1;
import com.example.shop.user.presentation.dto.response.ResPostAuthLoginDtoV1;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("dev")
class PaymentControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testPostPaymentsSuccess() throws Exception {
        String accessJwt = loginAndGetAccessToken();
        String orderId = createOrder(accessJwt);

        ReqPostPaymentsDtoV1 reqDto = ReqPostPaymentsDtoV1.builder()
                .payment(ReqPostPaymentsDtoV1.PaymentDto.builder()
                        .orderId(UUID.fromString(orderId))
                        .method(Method.CARD)
                        .transactionKey("test-transaction")
                        .build())
                .build();

        MvcResult mvcResult = mockMvc.perform(
                        post("/v1/payments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqDto))
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk(),
                        MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"),
                        MockMvcResultMatchers.jsonPath("$.data.payment.orderStatus").value(Order.Status.PAID.toString())
                )
                .andDo(
                        MockMvcRestDocumentationWrapper.document("결제 생성 성공",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("결제 V1")
                                        .summary("결제 처리")
                                        .description("""
                                                주문에 대한 결제를 완료합니다.
                                                
                                                ---
                                                
                                                """)
                                        .build()
                                )
                        )
                )
                .andReturn();

        ApiDto<ResPostPaymentsDtoV1> responseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(responseDto.getData().getPayment().getId()).isNotNull();
        assertThat(responseDto.getData().getPayment().getOrderId()).isEqualTo(orderId);
    }

    @Test
    void testGetPaymentsWithIdSuccess() throws Exception {
        String accessJwt = loginAndGetAccessToken();
        String orderId = createOrder(accessJwt);
        String paymentId = payOrder(accessJwt, orderId);

        MvcResult mvcResult = mockMvc.perform(
                        get("/v1/payments/{id}", paymentId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk(),
                        MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"),
                        MockMvcResultMatchers.jsonPath("$.data.payment.status").value(Status.COMPLETED.toString())
                )
                .andDo(
                        MockMvcRestDocumentationWrapper.document("결제 상세 조회 성공",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("결제 V1")
                                        .summary("결제 상세 조회")
                                        .description("""
                                                결제 상세를 조회합니다.
                                                
                                                ---
                                                
                                                """)
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("id").type(SimpleType.STRING).description("결제 ID")
                                        )
                                        .build()
                                )
                        )
                )
                .andReturn();

        ApiDto<ResGetPaymentsWithIdDtoV1> responseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(responseDto.getData().getPayment().getOrder().getStatus()).isEqualTo(Order.Status.PAID);
        assertThat(responseDto.getData().getPayment().getMethod()).isEqualTo(Method.CARD);
    }

    private String createOrder(String accessJwt) throws Exception {
        Product product = productRepository.findAll(Pageable.unpaged()).getContent().stream()
                .findFirst()
                .orElseThrow();

        ReqPostOrdersDtoV1 reqDto = ReqPostOrdersDtoV1.builder()
                .order(ReqPostOrdersDtoV1.OrderDto.builder()
                        .orderItemList(List.of(
                                ReqPostOrdersDtoV1.OrderDto.OrderItemDto.builder()
                                        .productId(product.getId())
                                        .quantity(1L)
                                        .build()
                        ))
                        .build())
                .build();

        MvcResult orderResult = mockMvc.perform(
                        post("/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqDto))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ApiDto<ResPostOrdersDtoV1> orderResponse = objectMapper.readValue(
                orderResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        return orderResponse.getData().getOrder().getId();
    }

    private String payOrder(String accessJwt, String orderId) throws Exception {
        ReqPostPaymentsDtoV1 reqDto = ReqPostPaymentsDtoV1.builder()
                .payment(ReqPostPaymentsDtoV1.PaymentDto.builder()
                        .orderId(UUID.fromString(orderId))
                        .method(Method.CARD)
                        .transactionKey("test-transaction")
                        .build())
                .build();

        MvcResult paymentResult = mockMvc.perform(
                        post("/v1/payments")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqDto))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ApiDto<ResPostPaymentsDtoV1> paymentResponse = objectMapper.readValue(
                paymentResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        return paymentResponse.getData().getPayment().getId();
    }

    private String loginAndGetAccessToken() throws Exception {
        ReqPostAuthLoginDtoV1 reqDto = ReqPostAuthLoginDtoV1.builder()
                .user(
                        ReqPostAuthLoginDtoV1.UserDto.builder()
                                .username("temp1")
                                .password("temp1234")
                                .build()
                )
                .build();

        MvcResult loginResult = mockMvc.perform(
                        post("/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqDto))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ApiDto<ResPostAuthLoginDtoV1> resLoginDto = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        return resLoginDto.getData().getAccessJwt();
    }
}
