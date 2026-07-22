package io.jieum.gyulbackendrepository.domain.report.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 대화 종료 후 FastAPI가 발행한 분석 리포트 적재 (docs/integration-spec.md §3.4)
@Getter
@Entity
@NoArgsConstructor
@Table(name = "INTERVIEW_REPORT")
public class InterviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // 대화 세션 고유 ID — 멱등 키 (중복 수신 방지)
    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;

    // 리포트 소유 회원 (email로 조회해 매핑)
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    // PHASE_1(자기분석) / PHASE_2(기술면접)
    @Column(name = "phase")
    private String phase;

    @Enumerated(EnumType.STRING)
    @Column(name = "dominant_emotion")
    private Emotion dominantEmotion;

    // 감정별 확률 맵을 JSON 문자열로 저장
    @Column(name = "emotion_scores", columnDefinition = "TEXT")
    private String emotionScores;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // 적재 시각
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public InterviewReport(String sessionId, Long memberId, String phase, Emotion dominantEmotion,
                           String emotionScores, String summary, LocalDateTime startedAt,
                           LocalDateTime endedAt, LocalDateTime createdAt) {
        this.sessionId = sessionId;
        this.memberId = memberId;
        this.phase = phase;
        this.dominantEmotion = dominantEmotion;
        this.emotionScores = emotionScores;
        this.summary = summary;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.createdAt = createdAt;
    }
}
