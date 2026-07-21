package io.jieum.gyulbackendrepository.domain.report.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Kafka 분석 리포트 메시지 (docs/integration-spec.md §3.1 스키마 v1).
 * 하위호환을 위해 알 수 없는 필드는 무시한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisReportMessage(
        int schemaVersion,
        String sessionId,
        String email,
        String phase,
        String startedAt,   // ISO-8601 (UTC)
        String endedAt,     // ISO-8601 (UTC)
        EmotionPayload emotion,
        String summary,
        Map<String, Object> metrics
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EmotionPayload(
            String dominant,
            Map<String, Double> scores
    ) {
    }
}
