package io.jieum.gyulbackendrepository.domain.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jieum.gyulbackendrepository.domain.report.model.dto.ReportResponseDto;
import io.jieum.gyulbackendrepository.domain.report.model.entity.InterviewReport;
import io.jieum.gyulbackendrepository.domain.report.repository.InterviewReportRepository;
import io.jieum.gyulbackendrepository.domain.user.model.entity.Member;
import io.jieum.gyulbackendrepository.domain.user.repository.MemberRepository;
import io.jieum.gyulbackendrepository.global.exception.BusinessException;
import io.jieum.gyulbackendrepository.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

// 로그인 회원 본인의 분석 리포트 조회 (docs/integration-spec.md §3.4)
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryService {

    private static final TypeReference<Map<String, Double>> SCORES_TYPE = new TypeReference<>() {
    };

    private final InterviewReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    // 본인 리포트 목록 (최신순)
    public List<ReportResponseDto> getMyReports(String email) {
        Long memberId = resolveMemberId(email);
        return reportRepository.findByMemberIdOrderByCreatedAtDesc(memberId).stream()
                .map(this::toDto)
                .toList();
    }

    // 본인 리포트 단건 상세 — 없으면 404, 타인 소유면 403
    public ReportResponseDto getMyReport(String email, Long reportId) {
        Long memberId = resolveMemberId(email);
        InterviewReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));
        if (!report.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.REPORT_FORBIDDEN);
        }
        return toDto(report);
    }

    private Long resolveMemberId(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return member.getId();
    }

    private ReportResponseDto toDto(InterviewReport report) {
        return new ReportResponseDto(
                report.getId(),
                report.getSessionId(),
                report.getPhase(),
                report.getDominantEmotion(),
                deserializeScores(report.getEmotionScores(), report.getId()),
                report.getSummary(),
                report.getStartedAt(),
                report.getEndedAt(),
                report.getCreatedAt()
        );
    }

    // JSON 문자열로 저장된 감정 점수를 Map으로 복원. 실패 시 빈 맵
    private Map<String, Double> deserializeScores(String json, Long reportId) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, SCORES_TYPE);
        } catch (JsonProcessingException e) {
            log.warn("emotion_scores 역직렬화 실패, 빈 값 반환: reportId={}", reportId);
            return Map.of();
        }
    }
}
