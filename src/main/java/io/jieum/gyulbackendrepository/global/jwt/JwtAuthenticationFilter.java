package io.jieum.gyulbackendrepository.global.jwt;

import io.jieum.gyulbackendrepository.global.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    // 토큰 검증 실패 사유를 EntryPoint로 전달하는 request attribute 키
    public static final String AUTH_ERROR_ATTRIBUTE = "jwtAuthError";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 헤더에서 토큰 추출
        String token = resolveToken(request);
        // 토큰이 있으면 검증 후 SecurityContext에 인증 정보 등록.
        // 실패 사유(만료/손상)는 attribute로 남겨 EntryPoint가 구분 응답하도록 한다.
        // (토큰이 아예 없으면 attribute 미설정 → EntryPoint가 AUTHENTICATION_REQUIRED 처리)
        if (StringUtils.hasText(token)) {
            try {
                Authentication auth = jwtProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (ExpiredJwtException e) {
                request.setAttribute(AUTH_ERROR_ATTRIBUTE, ErrorCode.TOKEN_EXPIRED);
            } catch (JwtException | IllegalArgumentException e) {
                request.setAttribute(AUTH_ERROR_ATTRIBUTE, ErrorCode.INVALID_TOKEN);
            }
        }
        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    // 토큰의 Bearer 접두사 제거 메서드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
