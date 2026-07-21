package io.jieum.gyulbackendrepository.domain.report.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jieum.gyulbackendrepository.domain.report.model.dto.AnalysisReportMessage;
import io.jieum.gyulbackendrepository.domain.report.service.AnalysisReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// FastAPI가 발행한 분석 리포트(JSON)를 수신 (docs/integration-spec.md §3)
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisReportListener {

    private final AnalysisReportService analysisReportService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topic.analysis-report}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(String payload) {
        AnalysisReportMessage message;
        try {
            message = objectMapper.readValue(payload, AnalysisReportMessage.class);
        } catch (Exception e) {
            // 스키마 파싱 실패는 재처리해도 실패하므로 스킵 (추후 DLQ 도입 — 명세서 §6 TODO)
            log.error("분석 리포트 역직렬화 실패 — 스킵: {}", e.getMessage());
            return;
        }
        analysisReportService.save(message);
    }
}
