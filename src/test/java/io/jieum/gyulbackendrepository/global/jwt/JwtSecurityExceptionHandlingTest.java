package io.jieum.gyulbackendrepository.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jieum.gyulbackendrepository.global.exception.ErrorCode;
import io.jieum.gyulbackendrepository.global.exception.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

// 스프링 컨텍스트 없이 필터 단계 EntryPoint/AccessDeniedHandler 응답을 검증
class JwtSecurityExceptionHandlingTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(objectMapper);
    private final JwtAccessDeniedHandler accessDeniedHandler = new JwtAccessDeniedHandler(objectMapper);

    private ErrorResponse parse(MockHttpServletResponse response) throws Exception {
        return objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
    }

    @Test
    void 토큰이_없으면_401_AUTHENTICATION_REQUIRED() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/members/me/reports");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, null);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).contains("application/json");
        ErrorResponse body = parse(response);
        assertThat(body.code()).isEqualTo("AUTHENTICATION_REQUIRED");
        assertThat(body.path()).isEqualTo("/api/v1/members/me/reports");
    }

    @Test
    void 필터가_남긴_만료_사유가_있으면_401_TOKEN_EXPIRED() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/members/me/reports");
        request.setAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE, ErrorCode.TOKEN_EXPIRED);
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, null);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(parse(response).code()).isEqualTo("TOKEN_EXPIRED");
    }

    @Test
    void 손상된_토큰_사유가_있으면_401_INVALID_TOKEN() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/members/me/profile");
        request.setAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE, ErrorCode.INVALID_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, null);

        assertThat(parse(response).code()).isEqualTo("INVALID_TOKEN");
    }

    @Test
    void 권한_부족은_403_ACCESS_DENIED() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/admin/something");
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(request, response, new AccessDeniedException("denied"));

        assertThat(response.getStatus()).isEqualTo(403);
        ErrorResponse body = parse(response);
        assertThat(body.code()).isEqualTo("ACCESS_DENIED");
        assertThat(body.status()).isEqualTo(403);
    }
}
