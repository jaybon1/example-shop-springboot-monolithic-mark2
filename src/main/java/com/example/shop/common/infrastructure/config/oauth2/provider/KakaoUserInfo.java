package com.example.shop.common.infrastructure.config.oauth2.provider;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return "";
    }

    @Override
    public String getEmail() {
        return "";
    }

    @Override
    public String getNickname() {
        return "";
    }

//    public KakaoMemberInfo(Map<String, Object> attributes) {
//        this.attributes = attributes;
//    }
//
//    @Override
//    public MemberSocialEntity.Provider getProvider() {
//        return MemberSocialEntity.Provider.KAKAO;
//    }
//
//    @Override
//    public String getProviderId() {
//        return String.valueOf(UtilFunction.getMapAttribute(attributes, "id"));
//    }
//
//    @Override
//    public String getEmail() {
//        return String.valueOf(UtilFunction.getMapAttribute(attributes, "kakao_account", "email"));
//    }
//
//    @Override
//    public String getNickname() {
//        return String.valueOf(UtilFunction.getMapAttribute(attributes, "properties", "nickname"));
//    }

}
