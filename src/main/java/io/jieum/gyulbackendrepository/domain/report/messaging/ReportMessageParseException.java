package io.jieum.gyulbackendrepository.domain.report.messaging;

/**
 * 분석 리포트 메시지 스키마 파싱 실패 예외.
 * 재처리해도 동일하게 실패하는 poison message이므로, 에러 핸들러가 재시도 없이 DLQ로 라우팅한다.
 * (docs/integration-spec.md §3.3)
 */
public class ReportMessageParseException extends RuntimeException {

    public ReportMessageParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
