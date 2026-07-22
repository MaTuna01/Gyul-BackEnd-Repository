package io.jieum.gyulbackendrepository.domain.report.model.dto;

import io.jieum.gyulbackendrepository.domain.report.model.entity.Emotion;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 분석 리포트 조회 응답 (docs/integration-spec.md §3.4).
 * 저장 시 JSON 문자열로 보관한 emotionScores는 응답에서 Map으로 역직렬화한다.
 */
public record ReportResponseDto(
        Long id,
        String sessionId,
        String phase,
        Emotion dominantEmotion,
        Map<String, Double> emotionScores,
        String summary,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        LocalDateTime createdAt
) {
}
