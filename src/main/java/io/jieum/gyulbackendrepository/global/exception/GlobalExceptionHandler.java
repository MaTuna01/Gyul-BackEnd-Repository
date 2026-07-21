package io.jieum.gyulbackendrepository.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

// 전역 예외 처리 — 도메인 오류를 표준 ErrorResponse로 변환
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 도메인 비즈니스 예외 → ErrorCode에 정의된 상태·코드로 변환
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e, HttpServletRequest request) {
        ErrorCode code = e.getErrorCode();
        log.warn("BusinessException: code={}, message={}, path={}", code.getCode(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(code.getStatus())
                .body(ErrorResponse.of(code, e.getMessage(), request.getRequestURI()));
    }

    // @Valid 요청 바디 검증 실패 → 400, 필드별 사유 포함
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        ErrorCode code = ErrorCode.INVALID_INPUT;
        log.warn("ValidationException: path={}, fieldErrors={}", request.getRequestURI(), fieldErrors);
        return ResponseEntity.status(code.getStatus())
                .body(ErrorResponse.of(code, code.getMessage(), request.getRequestURI(), fieldErrors));
    }

    // 미처리 예외 → 500 (내부 메시지는 노출하지 않음)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e, HttpServletRequest request) {
        ErrorCode code = ErrorCode.INTERNAL_ERROR;
        log.error("Unhandled exception: path={}", request.getRequestURI(), e);
        return ResponseEntity.status(code.getStatus())
                .body(ErrorResponse.of(code, code.getMessage(), request.getRequestURI()));
    }
}
