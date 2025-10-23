package com.example.shop.user.presentation.controller;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.shop.common.presentation.dto.ApiDto;
import com.example.shop.user.presentation.dto.request.ReqAuthPostRefreshDtoV1;
import com.example.shop.user.presentation.dto.request.ReqPostAuthLoginDtoV1;
import com.example.shop.user.presentation.dto.request.ReqPostAuthRegisterDtoV1;
import com.example.shop.user.presentation.dto.response.ResPostAuthLoginDtoV1;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
public class AuthControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testPostAuthRegisterSuccess() throws Exception {

        ReqPostAuthRegisterDtoV1 reqDto = ReqPostAuthRegisterDtoV1.builder()
                .user(
                        ReqPostAuthRegisterDtoV1.UserDto.builder()
                                .username("temp4")
                                .password("temp1234")
                                .email("temp4@temp.com")
                                .nickname("temp4")
                                .build()
                )
                .build();
        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqDto))
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk()
                )
                .andDo(
                        MockMvcRestDocumentationWrapper.document("인증 유저 회원가입 성공",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("인증 V1")
                                        .summary("인증 유저 회원가입")
                                        .description("""
                                                인증 유저 회원가입 엔드포인트 입니다.
                                                
                                                ---
                                                
                                                """)
                                        .build()
                                )
                        )
                );

    }

    @Test
    public void testPostAuthLoginSuccess() throws Exception {

        ReqPostAuthLoginDtoV1 reqDto = ReqPostAuthLoginDtoV1.builder()
                .user(
                        ReqPostAuthLoginDtoV1.UserDto.builder()
                                .username("temp1")
                                .password("temp1234")
                                .build()
                )
                .build();
        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqDto))
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk()
                )
                .andDo(
                        MockMvcRestDocumentationWrapper.document("인증 로그인 성공",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("인증 V1")
                                        .summary("인증 로그인")
                                        .description("""
                                                인증 로그인 엔드포인트 입니다.
                                                
                                                ---
                                                
                                                """)
                                        .build()
                                )
                        )
                );

    }

    @Test
    public void testPostAuthRefreshSuccess() throws Exception {

        MvcResult loginMvcResult = login();
        ApiDto<ResPostAuthLoginDtoV1> resLoginDto = objectMapper.readValue(
                loginMvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );
        ReqAuthPostRefreshDtoV1 reqDto = ReqAuthPostRefreshDtoV1.builder()
                .refreshJwt(resLoginDto.getData().getRefreshJwt())
                .build();
        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqDto))
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk()
                )
                .andDo(
                        MockMvcRestDocumentationWrapper.document("인증 리프레시 성공",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                        .tag("인증 V1")
                                        .summary("인증 리프레시")
                                        .description("""
                                                인증 리프레시 엔드포인트 입니다.
                                                
                                                ---
                                                
                                                """)
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
