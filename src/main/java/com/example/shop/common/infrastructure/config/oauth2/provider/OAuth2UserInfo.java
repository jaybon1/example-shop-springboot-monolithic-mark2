package com.example.shop.common.infrastructure.config.oauth2.provider;


public interface OAuth2UserInfo {

    String getProviderId();

    String getEmail();

    String getNickname();
}
