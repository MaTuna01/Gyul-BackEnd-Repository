package io.jieum.gyulbackendrepository.domain.auth.oauth2.userinfo;

import java.util.Map;

// 제공자마다 다른 사용자 정보 응답 구조를 공통 인터페이스로 추상화
public interface OAuth2UserInfo {

    // 제공자 내 사용자 고유 ID
    String getProviderId();

    // 제공자명 (google, kakao)
    String getProvider();

    String getEmail();

    String getName();

    Map<String, Object> getAttributes();
}
