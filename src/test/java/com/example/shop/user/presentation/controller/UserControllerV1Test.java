package com.example.shop.user.presentation.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import com.example.shop.common.presentation.dto.ApiDto;
import com.example.shop.user.presentation.dto.request.ReqPostAuthLoginDtoV1;
import com.example.shop.user.presentation.dto.response.ResPostAuthLoginDtoV1;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("dev")
public class UserControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetUsersSuccess() throws Exception {
        MvcResult loginMvcResult = login();
        ApiDto<ResPostAuthLoginDtoV1> resLoginDto = objectMapper.readValue(
                loginMvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );
        String accessJwt = resLoginDto.getData().getAccessJwt();
        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/v1/users")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk(),
                        MockMvcResultMatchers.jsonPath("$.code").value("SUCCESS"),
                        MockMvcResultMatchers.jsonPath("$.data.userPage.content").isArray()
                )
                .andDo(
                        MockMvcRestDocumentationWrapper.document("유저 목록 조회 성공",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("유저 V1")
                                        .summary("유저 목록 조회")
                                        .description("""
                                                유저 목록을 조회합니다.
                                                
                                                ---
                                                
                                                """)
                                        .queryParameters(
                                                ResourceDocumentation.parameterWithName("username").type(SimpleType.STRING).description("사용자명 부분 검색").optional(),
                                                ResourceDocumentation.parameterWithName("nickname").type(SimpleType.STRING).description("닉네임 부분 검색").optional(),
                                                ResourceDocumentation.parameterWithName("email").type(SimpleType.STRING).description("이메일 부분 검색").optional()
                                        )
                                        .build()
                                )
                        )
                );
    }

    @Test
    public void testGetUserSuccess() throws Exception {
        MvcResult loginMvcResult = login();
        ApiDto<ResPostAuthLoginDtoV1> resLoginDto = objectMapper.readValue(
                loginMvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );
        String accessJwt = resLoginDto.getData().getAccessJwt();
        DecodedJWT decodedJWT = JWT.decode(accessJwt);
        String userId = decodedJWT.getClaim("id").asString();
        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/v1/users/{id}", userId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk()
                )
                .andDo(
                        MockMvcRestDocumentationWrapper.document("유저 상세 조회 성공",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("유저 V1")
                                        .summary("유저 상세 조회")
                                        .description("""
                                                유저 상세를 조회합니다.
                                                
                                                ---
                                                
                                                """)
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("id").type(SimpleType.STRING).description("유저 ID")
                                        )
                                        .build()
                                )
                        )
                );

    }

    @Test
    public void testDeleteUserSuccess() throws Exception {
        MvcResult loginMvcResult = login();
        ApiDto<ResPostAuthLoginDtoV1> resLoginDto = objectMapper.readValue(
                loginMvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );
        String accessJwt = resLoginDto.getData().getAccessJwt();
        DecodedJWT decodedJWT = JWT.decode(accessJwt);
        String userId = decodedJWT.getClaim("id").asString();
        mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/v1/users/{id}", userId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessJwt)
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk()
                )
                .andDo(
                        MockMvcRestDocumentationWrapper.document("유저 삭제 성공",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("유저 V1")
                                        .summary("유저 삭제")
                                        .description("""
                                                유저를 삭제합니다.
                                                
                                                ---
                                                
                                                """)
                                        .pathParameters(
                                                ResourceDocumentation.parameterWithName("id").type(SimpleType.STRING).description("유저 ID")
                                        )
                                        .build()
                                )
                        )
                );

    }

    private MvcResult login() throws Exception {
        ReqPostAuthLoginDtoV1 reqDto = ReqPostAuthLoginDtoV1.builder()
                .user(
                        ReqPostAuthLoginDtoV1.UserDto.builder()
                                .username("temp1")
                                .password("temp1234")
                                .build()
                )
                .build();
        return mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqDto))
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk()
                ).andReturn();
    }

}
