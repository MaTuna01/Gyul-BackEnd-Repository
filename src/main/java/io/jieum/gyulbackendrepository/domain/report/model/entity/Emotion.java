package io.jieum.gyulbackendrepository.domain.report.model.entity;

// 감정 enum — 연동 명세서(docs/integration-spec.md §3.2) 합의값. 신규 값 추가 시 명세서와 동기화.
public enum Emotion {
    TENSION,     // 긴장
    CONFUSION,   // 당황
    CALM,        // 평온
    CONFIDENCE,  // 자신감
    NEUTRAL;     // 중립

    // 알 수 없는 값 수신 시 NEUTRAL로 처리 (컨슈머 중단 방지)
    public static Emotion from(String value) {
        if (value == null) {
            return NEUTRAL;
        }
        try {
            return Emotion.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return NEUTRAL;
        }
    }
}
