package com.example.shopmark2.product.presentation.controller;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import com.example.shopmark2.global.presentation.dto.ApiDto;
import com.example.shopmark2.product.domain.model.Product;
import com.example.shopmark2.product.domain.repository.ProductRepository;
import com.example.shopmark2.product.presentation.dto.request.ReqPostProductsDtoV1;
import com.example.shopmark2.product.presentation.dto.request.ReqPutProductsWithIdDtoV1;
import com.example.shopmark2.product.presentation.dto.response.ResPostProductsDtoV1;
import com.example.shopmark2.user.domain.model.User;
import com.example.shopmark2.user.domain.model.UserRole;
import com.example.shopmark2.user.domain.repository.UserRepository;
import com.example.shopmark2.user.presentation.dto.request.ReqPostAuthLoginDtoV1;
import com.example.shopmark2.user.presentation.dto.response.ResPostAuthLoginDtoV1;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("dev")
class ProductControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testGetProductsSuccess() throws Exception {
        String accessJwt = loginAndGetAccessTokenWithManager();

        mockMvc.perform(
                        get("/v1/products")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk(),
                        MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"),
                        MockMvcResultMatchers.jsonPath("$.data.productPage.content").isArray()
                )
                .andDo(
                        MockMvcRestDocumentationWrapper.document("상품 목록 조회 성공",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("상품 V1")
                                        .summary("상품 목록 조회")
                                        .description("""
                                                상품 목록을 조회합니다.
                                                
                                                ---
                                                
                                                """)
                                        .queryParameters(
                                                ResourceDocumentation.parameterWithName("name").type(SimpleType.STRING).description("상품명 부분 검색").optional()
                                        )
                                        .build()
                                )
                        )
                );
    }

    @Test
    void testGetProductsWithIdSuccess() throws Exception {
        String accessJwt = loginAndGetAccessTokenWithManager();
        Product product = productRepository.findAll(Pageable.unpaged()).getContent().stream()
                .findFirst()
                .orElseThrow();

        mockMvc.perform(
                        get("/v1/products/{id}", product.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk(),
                        MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"),
                        MockMvcResultMatchers.jsonPath("$.data.product.id").value(product.getId().toString())
                )
                .andDo(
                        MockMvcRestDocumentationWrapper.document("상품 상세 조회 성공",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("상품 V1")
                                        .summary("상품 상세 조회")
                                        .description("""
                                                단일 상품을 조회합니다.
                                                
                                                ---
                                                
                                                """)
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("id").type(SimpleType.STRING).description("상품 ID")
                                        )
                                        .build()
                                )
                        )
                );
    }

    @Test
    void testPostProductsSuccess() throws Exception {
        String accessJwt = loginAndGetAccessTokenWithManager();
        String productName = "test-product-" + UUID.randomUUID();

        ReqPostProductsDtoV1 reqDto = ReqPostProductsDtoV1.builder()
                .product(
                        ReqPostProductsDtoV1.ProductDto.builder()
                                .name(productName)
                                .price(12000L)
                                .stock(50L)
                                .build()
                )
                .build();

        MvcResult mvcResult = mockMvc.perform(
                        post("/v1/products")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqDto))
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk(),
                        MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"),
                        MockMvcResultMatchers.jsonPath("$.data.product.id").isNotEmpty()
                )
                .andDo(
                        MockMvcRestDocumentationWrapper.document("상품 등록 성공",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("상품 V1")
                                        .summary("상품 등록")
                                        .description("""
                                                상품을 신규로 등록합니다.
                                                
                                                ---
                                                
                                                """)
                                        .build()
                                )
                        )
                )
                .andReturn();

        ApiDto<ResPostProductsDtoV1> responseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        productRepository.findById(UUID.fromString(responseDto.getData().getProduct().getId()))
                .orElseThrow();
    }

    @Test
    void testPutProductsWithIdSuccess() throws Exception {
        String accessJwt = loginAndGetAccessTokenWithManager();
        String productId = createProduct(accessJwt);
        String updatedName = "updated-product-" + UUID.randomUUID();

        ReqPutProductsWithIdDtoV1 reqDto = ReqPutProductsWithIdDtoV1.builder()
                .product(
                        ReqPutProductsWithIdDtoV1.ProductDto.builder()
                                .name(updatedName)
                                .price(15000L)
                                .stock(70L)
                                .build()
                )
                .build();

        mockMvc.perform(
                        put("/v1/products/{id}", productId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqDto))
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk(),
                        MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"),
                        MockMvcResultMatchers.jsonPath("$.data.product.id").value(productId)
                )
                .andDo(
                        MockMvcRestDocumentationWrapper.document("상품 수정 성공",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("상품 V1")
                                        .summary("상품 수정")
                                        .description("""
                                                상품 정보를 수정합니다.
                                                
                                                ---
                                                
                                                """)
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("id").type(SimpleType.STRING).description("상품 ID")
                                        )
                                        .build()
                                )
                        )
                );

        Product updatedProduct = productRepository.findById(UUID.fromString(productId))
                .orElseThrow();
        assertThat(updatedProduct.getName()).isEqualTo(updatedName);
        assertThat(updatedProduct.getPrice()).isEqualTo(15000L);
        assertThat(updatedProduct.getStock()).isEqualTo(70L);
    }

    @Test
    void testDeleteProductsWithIdSuccess() throws Exception {
        String accessJwt = loginAndGetAccessTokenWithManager();
        String productId = createProduct(accessJwt);

        mockMvc.perform(
                        delete("/v1/products/{id}", productId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk(),
                        MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"),
                        MockMvcResultMatchers.jsonPath("$.message").value("상품 삭제가 완료되었습니다.")
                )
                .andDo(
                        MockMvcRestDocumentationWrapper.document("상품 삭제 성공",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("상품 V1")
                                        .summary("상품 삭제")
                                        .description("""
                                                상품을 삭제합니다.
                                                
                                                ---
                                                
                                                """)
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("id").type(SimpleType.STRING).description("상품 ID")
                                        )
                                        .build()
                                )
                        )
                );

        Product deletedProduct = productRepository.findById(UUID.fromString(productId))
                .orElseThrow();
        assertThat(deletedProduct.getDeletedAt()).isNotNull();
    }

    private String loginAndGetAccessTokenWithManager() throws Exception {
        ensureManagerRole("temp1");
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

    private String createProduct(String accessJwt) throws Exception {
        ensureManagerRole("temp1");
        String productName = "helper-product-" + UUID.randomUUID();

        ReqPostProductsDtoV1 reqDto = ReqPostProductsDtoV1.builder()
                .product(
                        ReqPostProductsDtoV1.ProductDto.builder()
                                .name(productName)
                                .price(5000L)
                                .stock(30L)
                                .build()
                )
                .build();

        MvcResult mvcResult = mockMvc.perform(
                        post("/v1/products")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqDto))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ApiDto<ResPostProductsDtoV1> responseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        return responseDto.getData().getProduct().getId();
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
