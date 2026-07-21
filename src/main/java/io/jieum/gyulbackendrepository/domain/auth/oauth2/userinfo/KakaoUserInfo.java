package io.jieum.gyulbackendrepository.domain.auth.oauth2.userinfo;

import java.util.Map;

// Kakao 사용자 정보 응답 (id 최상위, 이메일/닉네임은 kakao_account 하위에 중첩)
public class KakaoUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        // user-name-attribute=id, Long으로 내려오므로 문자열화
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            return null;
        }
        return (String) kakaoAccount.get("email");
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getName() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            return null;
        }
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile == null) {
            return null;
        }
        return (String) profile.get("nickname");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
