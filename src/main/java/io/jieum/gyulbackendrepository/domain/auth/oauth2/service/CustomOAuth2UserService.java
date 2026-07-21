package io.jieum.gyulbackendrepository.domain.auth.oauth2.service;

import io.jieum.gyulbackendrepository.domain.auth.oauth2.userinfo.OAuth2UserInfo;
import io.jieum.gyulbackendrepository.domain.auth.oauth2.userinfo.OAuth2UserInfoFactory;
import io.jieum.gyulbackendrepository.domain.user.model.entity.Member;
import io.jieum.gyulbackendrepository.domain.user.model.entity.Role;
import io.jieum.gyulbackendrepository.domain.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 소셜 로그인 후처리 — 이메일 기준 계정 병합.
 * <ul>
 *   <li>동일 이메일 회원 존재: provider/providerId 연결(병합)</li>
 *   <li>신규: gender=null 상태로 Member 생성 → 이후 추가정보 입력 필요</li>
 * </ul>
 * 반환하는 OAuth2User의 name 속성 키는 "email"로 통일하여 SuccessHandler가 사용자를 식별한다.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.of(registrationId, oAuth2User.getAttributes());

        String email = userInfo.getEmail();
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("소셜 계정에서 이메일을 가져오지 못했습니다. 이메일 제공 동의가 필요합니다.");
        }

        // 이메일 기준 계정 병합: 기존 회원이면 provider 연결, 없으면 신규 생성
        Member member = memberRepository.findByEmail(email)
                .map(existing -> {
                    if (existing.getProvider() == null) {
                        existing.linkOAuth(userInfo.getProvider(), userInfo.getProviderId());
                    }
                    return existing;
                })
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .email(email)
                                .name(userInfo.getName())
                                .createdAt(LocalDateTime.now())
                                .gender(null) // 최초 가입 시 추가정보 입력 전까지 null
                                .role(Role.MEMBER)
                                .provider(userInfo.getProvider())
                                .providerId(userInfo.getProviderId())
                                .build()
                ));

        // name 속성 키를 "email"로 통일하기 위해 email을 최상위에 포함
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("email", email);

        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())),
                attributes,
                "email"
        );
    }
}
