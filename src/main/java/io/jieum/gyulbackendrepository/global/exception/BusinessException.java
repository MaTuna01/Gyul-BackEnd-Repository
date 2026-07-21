package io.jieum.gyulbackendrepository.global.exception;

import lombok.Getter;

/**
 * 도메인 비즈니스 예외. {@link ErrorCode}를 실어 전역 핸들러가 HTTP 상태·응답 코드로 변환한다.
 * 예외 클래스를 도메인마다 만들지 않고 ErrorCode 하나로 관리한다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 기본 메시지 대신 상황별 메시지를 실어야 할 때
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
