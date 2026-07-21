package io.jieum.gyulbackendrepository.domain.auth.oauth2.handler;

import io.jieum.gyulbackendrepository.domain.user.model.entity.Member;
import io.jieum.gyulbackendrepository.domain.user.repository.MemberRepository;
import io.jieum.gyulbackendrepository.global.jwt.JwtProperties;
import io.jieum.gyulbackendrepository.global.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 소셜 로그인 성공 시:
 * <ol>
 *   <li>JWT Access/Refresh 발급, Refresh Token은 Redis 저장(RT:{email})</li>
 *   <li>gender == null(추가정보 미입력) → 추가정보 입력 페이지로 리다이렉트</li>
 *   <li>gender != null → 메인 페이지로 리다이렉트</li>
 * </ol>
 * 토큰은 프론트가 수신하도록 리다이렉트 URL 쿼리 파라미터로 전달한다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String RT_KEY_PREFIX = "RT:";

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("소셜 로그인 처리 중 회원을 찾지 못했습니다: " + email));

        // JWT 발급
        String accessToken = jwtProvider.generateAccessToken(member);
        String refreshToken = jwtProvider.generateRefreshToken(email);

        // Refresh Token → Redis 저장 (Form 로그인과 동일 정책)
        redisTemplate.opsForValue().set(
                RT_KEY_PREFIX + email,
                refreshToken,
                jwtProperties.getRefreshExpiration(),
                TimeUnit.MILLISECONDS
        );

        // gender 미입력 시 추가정보 입력 페이지, 아니면 메인 페이지
        boolean needMoreInfo = member.getGender() == null;
        String targetPath = needMoreInfo ? "/oauth/additional-info" : "/";

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .path(targetPath)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
