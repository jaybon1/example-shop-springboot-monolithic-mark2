package com.example.shop.order.presentation.controller;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import com.example.shop.common.presentation.dto.ApiDto;
import com.example.shop.order.domain.model.Order;
import com.example.shop.order.presentation.dto.request.ReqPostOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResPostOrdersDtoV1;
import com.example.shop.product.domain.model.Product;
import com.example.shop.product.domain.repository.ProductRepository;
import com.example.shop.user.domain.model.User;
import com.example.shop.user.domain.model.UserRole;
import com.example.shop.user.domain.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("dev")
class OrderControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testPostOrdersSuccess() throws Exception {
        String accessJwt = loginAndGetAccessToken("temp1");

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

        MvcResult mvcResult = mockMvc.perform(
                        post("/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqDto))
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk(),
                        MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"),
                        MockMvcResultMatchers.jsonPath("$.data.order.status").value(Order.Status.CREATED.toString())
                )
                .andDo(
                        MockMvcRestDocumentationWrapper.document("주문 생성 성공",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("주문 V1")
                                        .summary("주문 생성")
                                        .description("""
                                                주문을 생성합니다. 결제는 별도 요청으로 진행됩니다.
                                                
                                                ---
                                                
                                                """)
                                        .build()
                                )
                        )
                )
                .andReturn();

        ApiDto<ResPostOrdersDtoV1> responseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(responseDto.getData().getOrder().getPayment()).isNull();
    }

    @Test
    void testGetOrdersSuccess() throws Exception {
        String accessJwt = loginAndGetAccessToken("temp1");
        createOrder(accessJwt);

        mockMvc.perform(
                        get("/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk(),
                        MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"),
                        MockMvcResultMatchers.jsonPath("$.data.orderPage.content").isArray()
                )
                .andDo(
                        MockMvcRestDocumentationWrapper.document("주문 목록 조회 성공",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("주문 V1")
                                        .summary("주문 목록 조회")
                                        .description("""
                                                주문 목록을 조회합니다.
                                                
                                                ---
                                                
                                                """)
                                        .build()
                                )
                        )
                );
    }

    @Test
    void testGetOrderSuccess() throws Exception {
        String accessJwt = loginAndGetAccessToken("temp1");
        String orderId = createOrder(accessJwt);

        mockMvc.perform(
                        get("/v1/orders/{id}", orderId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk(),
                        MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"),
                        MockMvcResultMatchers.jsonPath("$.data.order.id").value(orderId)
                )
                .andDo(
                        MockMvcRestDocumentationWrapper.document("주문 상세 조회 성공",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("주문 V1")
                                        .summary("주문 상세 조회")
                                        .description("""
                                                단일 주문을 조회합니다.
                                                
                                                ---
                                                
                                                """)
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("id").type(SimpleType.STRING).description("주문 ID")
                                        )
                                        .build()
                                )
                        )
                );
    }

    @Test
    void testPostOrdersCancelSuccess() throws Exception {
        String accessJwt = loginAndGetAccessToken("temp1");
        String orderId = createOrder(accessJwt);

        mockMvc.perform(
                        post("/v1/orders/{id}/cancel", orderId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk(),
                        MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"),
                        MockMvcResultMatchers.jsonPath("$.message").value("주문이 취소되었습니다.")
                )
                .andDo(
                        MockMvcRestDocumentationWrapper.document("주문 취소 성공",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("주문 V1")
                                        .summary("주문 취소")
                                        .description("""
                                                생성된 주문을 취소합니다.
                                                
                                                ---
                                                
                                                """)
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("id").type(SimpleType.STRING).description("주문 ID")
                                        )
                                        .build()
                                )
                        )
                );

        mockMvc.perform(
                        get("/v1/orders/{id}", orderId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk(),
                        MockMvcResultMatchers.jsonPath("$.data.order.status").value(Order.Status.CANCELLED.toString())
                );
    }

    @Test
    void testPostOrdersCancelByManagerSuccess() throws Exception {
        String buyerAccessJwt = loginAndGetAccessToken("temp2");
        String orderId = createOrder(buyerAccessJwt);

        ensureManagerRole("temp1");
        String managerAccessJwt = loginAndGetAccessToken("temp1");

        mockMvc.perform(
                        post("/v1/orders/{id}/cancel", orderId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerAccessJwt)
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk(),
                        MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"),
                        MockMvcResultMatchers.jsonPath("$.message").value("주문이 취소되었습니다.")
                );

        mockMvc.perform(
                        get("/v1/orders/{id}", orderId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerAccessJwt)
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk(),
                        MockMvcResultMatchers.jsonPath("$.data.order.status").value(Order.Status.CANCELLED.toString())
                );
    }

    private String loginAndGetAccessToken(String username) throws Exception {
        ReqPostAuthLoginDtoV1 reqDto = ReqPostAuthLoginDtoV1.builder()
                .user(
                        ReqPostAuthLoginDtoV1.UserDto.builder()
                                .username(username)
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

        MvcResult mvcResult = mockMvc.perform(
                        post("/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqDto))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ApiDto<ResPostOrdersDtoV1> responseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        return responseDto.getData().getOrder().getId();
    }

    private void ensureManagerRole(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow();
        if (user.hasRole(UserRole.Role.MANAGER)) {
            return;
        }
        User updatedUser = user.addRole(UserRole.builder()
                .id(null)
                .role(UserRole.Role.MANAGER)
                .build());
        userRepository.save(updatedUser);
    }
}
