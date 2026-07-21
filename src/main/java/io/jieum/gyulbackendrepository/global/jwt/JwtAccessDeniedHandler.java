package io.jieum.gyulbackendrepository.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jieum.gyulbackendrepository.global.exception.ErrorCode;
import io.jieum.gyulbackendrepository.global.exception.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증은 되었으나 권한이 부족한 요청(403)에 표준 에러 응답을 반환한다.
 */
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ErrorResponseWriter.write(response, objectMapper, ErrorCode.ACCESS_DENIED, request.getRequestURI());
    }
}
