package io.jieum.gyulbackendrepository.domain.report.controller;

import io.jieum.gyulbackendrepository.domain.report.model.dto.ReportResponseDto;
import io.jieum.gyulbackendrepository.domain.report.service.ReportQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 분석 리포트 조회 — JWT 인증 필요, principal은 이메일 (docs/integration-spec.md §3.4)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me/reports")
public class ReportController {

    private final ReportQueryService reportQueryService;

    // 본인 리포트 목록 (최신순)
    @GetMapping
    public ResponseEntity<List<ReportResponseDto>> getMyReports(
            @AuthenticationPrincipal String email
    ) {
        return ResponseEntity.ok(reportQueryService.getMyReports(email));
    }

    // 본인 리포트 단건 상세
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponseDto> getMyReport(
            @AuthenticationPrincipal String email,
            @PathVariable Long reportId
    ) {
        return ResponseEntity.ok(reportQueryService.getMyReport(email, reportId));
    }
}
