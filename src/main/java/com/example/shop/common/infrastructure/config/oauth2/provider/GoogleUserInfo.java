package com.example.shop.common.infrastructure.config.oauth2.provider;

import java.util.Map;

public class GoogleUserInfo implements OAuth2UserInfo {


    private Map<String, Object> attributes;

    public GoogleUserInfo(Map<String, Object> attributes) {
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

    //    @Override
//    public MemberSocialEntity.Provider getProvider() {
//        return MemberSocialEntity.Provider.GOOGLE;
//    }
//
//    @Override
//    public String getProviderId() {
//        return String.valueOf(UtilFunction.getMapAttribute(attributes, "sub"));
//    }
//
//    @Override
//    public String getEmail() {
//        return String.valueOf(UtilFunction.getMapAttribute(attributes, "name"));
//    }
//
//    @Override
//    public String getNickname() {
//        return String.valueOf(UtilFunction.getMapAttribute(attributes, "email"));
//    }

}
