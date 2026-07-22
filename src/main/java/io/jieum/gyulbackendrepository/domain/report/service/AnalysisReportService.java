package io.jieum.gyulbackendrepository.domain.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jieum.gyulbackendrepository.domain.report.model.dto.AnalysisReportMessage;
import io.jieum.gyulbackendrepository.domain.report.model.entity.Emotion;
import io.jieum.gyulbackendrepository.domain.report.model.entity.InterviewReport;
import io.jieum.gyulbackendrepository.domain.report.repository.InterviewReportRepository;
import io.jieum.gyulbackendrepository.domain.user.model.entity.Member;
import io.jieum.gyulbackendrepository.domain.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

// 분석 리포트 메시지를 검증·매핑하여 적재 (docs/integration-spec.md §3.3)
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisReportService {

    private final InterviewReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void save(AnalysisReportMessage message) {
        // 멱등: 동일 sessionId면 무시 (Kafka at-least-once 대비)
        if (message.sessionId() == null || reportRepository.existsBySessionId(message.sessionId())) {
            log.info("이미 적재되었거나 sessionId 없는 리포트 — 스킵: sessionId={}", message.sessionId());
            return;
        }

        // 미존재 이메일이면 스킵 (컨슈머 중단 방지)
        Member member = memberRepository.findByEmail(message.email()).orElse(null);
        if (member == null) {
            log.warn("존재하지 않는 회원의 리포트 수신 — 스킵: email={}, sessionId={}",
                    message.email(), message.sessionId());
            return;
        }

        InterviewReport report = InterviewReport.builder()
                .sessionId(message.sessionId())
                .memberId(member.getId())
                .phase(message.phase())
                .dominantEmotion(resolveDominant(message))
                .emotionScores(serializeScores(message))
                .summary(message.summary())
                .startedAt(parseUtc(message.startedAt()))
                .endedAt(parseUtc(message.endedAt()))
                .createdAt(LocalDateTime.now())
                .build();

        reportRepository.save(report);
        log.info("분석 리포트 적재 완료: sessionId={}, memberId={}", message.sessionId(), member.getId());
    }

    private Emotion resolveDominant(AnalysisReportMessage message) {
        return message.emotion() == null ? Emotion.NEUTRAL : Emotion.from(message.emotion().dominant());
    }

    private String serializeScores(AnalysisReportMessage message) {
        Map<String, Double> scores = message.emotion() == null ? Map.of() : message.emotion().scores();
        if (scores == null) {
            scores = Map.of();
        }
        try {
            return objectMapper.writeValueAsString(scores);
        } catch (JsonProcessingException e) {
            log.warn("emotion.scores 직렬화 실패, 빈 값으로 저장: sessionId={}", message.sessionId());
            return "{}";
        }
    }

    // ISO-8601(UTC) 문자열 → LocalDateTime(UTC). 파싱 실패 시 null
    private LocalDateTime parseUtc(String iso) {
        if (iso == null || iso.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(iso), ZoneOffset.UTC);
        } catch (Exception e) {
            log.warn("시각 파싱 실패, null로 저장: value={}", iso);
            return null;
        }
    }
}
