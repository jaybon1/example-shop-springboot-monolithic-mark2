package com.example.shop.global.infrastructure.config.oauth2.provider;


public interface OAuth2UserInfo {

    String getProviderId();

    String getEmail();

    String getNickname();
}
