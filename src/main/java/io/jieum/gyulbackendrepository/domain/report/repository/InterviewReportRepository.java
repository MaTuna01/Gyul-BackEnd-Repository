package io.jieum.gyulbackendrepository.domain.report.repository;

import io.jieum.gyulbackendrepository.domain.report.model.entity.InterviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewReportRepository extends JpaRepository<InterviewReport, Long> {

    boolean existsBySessionId(String sessionId);

    // 회원 본인 리포트 목록 (최신 적재순)
    List<InterviewReport> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    // 단건 상세 — 본인 소유만 조회되도록 memberId 함께 검증
    Optional<InterviewReport> findByIdAndMemberId(Long id, Long memberId);
}
