//package com.example.shopmark1.global.infrastructure.config.oauth2;
//
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class CustomOAuth2UserService extends DefaultOAuth2UserService {
//
//    private final PasswordEncoder passwordEncoder;
//    private final MemberRepository memberRepository;
//    private final MemberSocialRepository memberSocialRepository;
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//
//        OAuth2User oAuth2User = super.loadUser(userRequest);
//        OAuth2MemberInfo oAuth2MemberInfo;
//        if (userRequest.getClientRegistration().getRegistrationId().equals("kakao")) {
//            oAuth2MemberInfo = new KakaoMemberInfo(oAuth2User.getAttributes());
//        } else {
//            throw new AuthenticationException("지원하지 않는 OAuth2 제공자입니다. 현재 지원되는 제공자는 카카오입니다.");
//        }
//        Optional<MemberSocialEntity> memberSocialEntityOptional = memberSocialRepository.findByProviderAndProviderId(
//                oAuth2MemberInfo.getProvider(),
//                oAuth2MemberInfo.getProviderId()
//        );
//        if (memberSocialEntityOptional.isEmpty()) {
//            String providerWithId = oAuth2MemberInfo.getProvider() + "_" + oAuth2MemberInfo.getProviderId();
//            MemberEntity memberEntity = MemberEntity.builder()
//                    .username(providerWithId)
//                    .password(passwordEncoder.encode(providerWithId + "_" + Math.random()))
//                    .nickname(oAuth2MemberInfo.getNickname() != null ? oAuth2MemberInfo.getNickname() : providerWithId)
//                    .email(oAuth2MemberInfo.getEmail())
//                    .memberRoleList(new ArrayList<>())
//                    .memberSocialList(new ArrayList<>())
//                    .build();
//            MemberRoleEntity memberRoleEntity = MemberRoleEntity.builder()
//                    .role(MemberRoleEntity.Role.MEMBER)
//                    .build();
//            MemberSocialEntity memberSocialEntity = MemberSocialEntity.builder()
//                    .provider(oAuth2MemberInfo.getProvider())
//                    .providerId(oAuth2MemberInfo.getProviderId())
//                    .nickname(oAuth2MemberInfo.getNickname())
//                    .email(oAuth2MemberInfo.getEmail())
//                    .build();
//            memberEntity.add(memberRoleEntity);
//            memberEntity.add(memberSocialEntity);
//            memberRepository.save(memberEntity);
//            return CustomUserDetails.of(memberEntity);
//        }
//        return CustomUserDetails.of(memberSocialEntityOptional.get().getMember());
//
//    }
//
//}
