package io.jieum.gyulbackendrepository.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jieum.gyulbackendrepository.global.exception.ErrorCode;
import io.jieum.gyulbackendrepository.global.exception.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증되지 않은 요청이 보호 자원에 접근할 때(401) 표준 에러 응답을 반환한다.
 * 필터가 남긴 사유(만료/손상)가 있으면 그대로, 없으면 인증 필요로 응답한다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorCode errorCode = (ErrorCode) request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE);
        if (errorCode == null) {
            errorCode = ErrorCode.AUTHENTICATION_REQUIRED;
        }
        ErrorResponseWriter.write(response, objectMapper, errorCode, request.getRequestURI());
    }
}
