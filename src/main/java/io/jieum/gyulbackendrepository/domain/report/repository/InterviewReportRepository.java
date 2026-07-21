package io.jieum.gyulbackendrepository.domain.report.repository;

import io.jieum.gyulbackendrepository.domain.report.model.entity.InterviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewReportRepository extends JpaRepository<InterviewReport, Long> {

    boolean existsBySessionId(String sessionId);
}
