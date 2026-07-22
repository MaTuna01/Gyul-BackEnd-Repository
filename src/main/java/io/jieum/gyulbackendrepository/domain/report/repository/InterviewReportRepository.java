package io.jieum.gyulbackendrepository.domain.report.repository;

import io.jieum.gyulbackendrepository.domain.report.model.entity.InterviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewReportRepository extends JpaRepository<InterviewReport, Long> {

    boolean existsBySessionId(String sessionId);

    // 회원 본인 리포트 목록 (최신 적재순)
    List<InterviewReport> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
