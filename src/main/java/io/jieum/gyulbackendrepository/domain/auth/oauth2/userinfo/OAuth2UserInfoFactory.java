package io.jieum.gyulbackendrepository.domain.auth.oauth2.userinfo;

import java.util.Map;

// registrationId(google/kakao)에 맞는 OAuth2UserInfo 구현체 생성
public class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {
    }

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleUserInfo(attributes);
            case "kakao" -> new KakaoUserInfo(attributes);
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다: " + registrationId);
        };
    }
}
