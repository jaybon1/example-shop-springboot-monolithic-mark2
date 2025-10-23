package com.example.shop.user.infrastructure.api.kakao.repository;

import com.example.shop.global.util.UtilFunction;
import com.example.shop.user.infrastructure.api.kakao.dto.response.ResGetKakaoUserMeDtoV2;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Repository
@RequiredArgsConstructor
public class KakaoApiRepository {

    private final ObjectMapper objectMapper;

    public synchronized ResGetKakaoUserMeDtoV2 getKakaoUserMeV2(
            String accessToken
    ) {
        final UriComponents uriComponents = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com/v2/user/me")
                .build();
        ResponseEntity<String> responseEntity = UtilFunction.getWebClientAutoRedirect("json")
                .get()
                .uri(uriComponents.toUriString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                .block();
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("카카오 유저 정보 API 호출에 실패했습니다. " + responseEntity.getBody());
        }
        ResGetKakaoUserMeDtoV2 resGetKakaoUserMeDtoV2;
        try {
            resGetKakaoUserMeDtoV2 = objectMapper.readValue(responseEntity.getBody(), ResGetKakaoUserMeDtoV2.class);
        } catch (Exception e) {
            throw new RuntimeException("카카오 유저 정보 API 호출에 실패했습니다. " + e.getMessage());
        }
        return resGetKakaoUserMeDtoV2;
    }

}
