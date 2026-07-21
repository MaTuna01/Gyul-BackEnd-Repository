package io.jieum.gyulbackendrepository.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 필터 단계(시큐리티 EntryPoint/AccessDeniedHandler)에서 표준 {@link ErrorResponse}를 직접 써 내려간다.
 * 컨트롤러 진입 전 예외라 @RestControllerAdvice가 잡지 못하므로 응답을 수동으로 작성한다.
 */
public final class ErrorResponseWriter {

    private ErrorResponseWriter() {
    }

    public static void write(HttpServletResponse response, ObjectMapper objectMapper, ErrorCode errorCode, String path)
            throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ErrorResponse body = ErrorResponse.of(errorCode, errorCode.getMessage(), path);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
