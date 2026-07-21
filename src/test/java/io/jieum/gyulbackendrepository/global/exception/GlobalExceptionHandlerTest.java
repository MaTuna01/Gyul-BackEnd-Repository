package io.jieum.gyulbackendrepository.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

// 스프링 컨텍스트 없이 핸들러 매핑만 검증하는 경량 단위 테스트
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private HttpServletRequest request(String uri) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI(uri);
        return req;
    }

    @Test
    void BusinessException은_ErrorCode의_상태와_코드로_변환된다() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBusiness(new BusinessException(ErrorCode.REPORT_FORBIDDEN), request("/api/v1/members/me/reports/1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("REPORT_FORBIDDEN");
        assertThat(response.getBody().status()).isEqualTo(403);
        assertThat(response.getBody().path()).isEqualTo("/api/v1/members/me/reports/1");
        assertThat(response.getBody().fieldErrors()).isNull();
    }

    @Test
    void 이메일_중복은_409로_변환된다() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBusiness(new BusinessException(ErrorCode.EMAIL_DUPLICATED), request("/api/v1/signup"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("EMAIL_DUPLICATED");
    }

    @Test
    void 미처리_예외는_500_INTERNAL_ERROR로_변환되고_내부메시지를_노출하지_않는다() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUnexpected(new RuntimeException("DB 커넥션 터짐"), request("/api/v1/anything"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().message()).doesNotContain("DB 커넥션");
    }
}
