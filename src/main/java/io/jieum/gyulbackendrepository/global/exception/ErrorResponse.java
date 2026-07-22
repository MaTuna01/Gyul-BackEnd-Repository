package io.jieum.gyulbackendrepository.global.exception;

import java.util.List;

/**
 * 표준 에러 응답 바디.
 * {@code fieldErrors}는 @Valid 검증 실패 시에만 채워지고, 그 외에는 null(직렬화 생략 안 함).
 */
public record ErrorResponse(
        String code,
        String message,
        int status,
        String path,
        List<FieldError> fieldErrors
) {

    public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
        return new ErrorResponse(errorCode.getCode(), message, errorCode.getStatus().value(), path, null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String path, List<FieldError> fieldErrors) {
        return new ErrorResponse(errorCode.getCode(), message, errorCode.getStatus().value(), path, fieldErrors);
    }

    // 검증 실패 필드 상세
    public record FieldError(String field, String reason) {
    }
}
