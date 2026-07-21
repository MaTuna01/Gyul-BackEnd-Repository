package io.jieum.gyulbackendrepository.domain.report;

import io.jieum.gyulbackendrepository.domain.report.model.dto.ReportResponseDto;
import io.jieum.gyulbackendrepository.domain.report.model.entity.Emotion;
import io.jieum.gyulbackendrepository.domain.report.model.entity.InterviewReport;
import io.jieum.gyulbackendrepository.domain.report.repository.InterviewReportRepository;
import io.jieum.gyulbackendrepository.domain.report.service.ReportQueryService;
import io.jieum.gyulbackendrepository.domain.user.model.entity.Member;
import io.jieum.gyulbackendrepository.domain.user.model.entity.Role;
import io.jieum.gyulbackendrepository.domain.user.repository.MemberRepository;
import io.jieum.gyulbackendrepository.global.exception.BusinessException;
import io.jieum.gyulbackendrepository.global.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class ReportQueryServiceTest {

    @Autowired
    private ReportQueryService reportQueryService;
    @Autowired
    private InterviewReportRepository reportRepository;
    @Autowired
    private MemberRepository memberRepository;

    private static final String EMAIL = "query-test@example.com";
    private static final String OTHER_EMAIL = "query-other@example.com";

    private Member member;
    private Member other;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
        cleanMembers();
        member = saveMember(EMAIL);
        other = saveMember(OTHER_EMAIL);
    }

    @AfterEach
    void tearDown() {
        reportRepository.deleteAll();
        cleanMembers();
    }

    private void cleanMembers() {
        memberRepository.findByEmail(EMAIL).ifPresent(memberRepository::delete);
        memberRepository.findByEmail(OTHER_EMAIL).ifPresent(memberRepository::delete);
    }

    private Member saveMember(String email) {
        return memberRepository.save(Member.builder()
                .email(email)
                .password("x")
                .name("조회테스트")
                .createdAt(LocalDateTime.now())
                .role(Role.MEMBER)
                .build());
    }

    private InterviewReport saveReport(String sessionId, Long memberId, LocalDateTime createdAt) {
        return reportRepository.save(InterviewReport.builder()
                .sessionId(sessionId)
                .memberId(memberId)
                .phase("PHASE_1")
                .dominantEmotion(Emotion.TENSION)
                .emotionScores("{\"tension\":0.62,\"calm\":0.38}")
                .summary("요약")
                .startedAt(LocalDateTime.now())
                .endedAt(LocalDateTime.now())
                .createdAt(createdAt)
                .build());
    }

    @Test
    void 본인_리포트를_최신순으로_조회한다() {
        saveReport("s-old", member.getId(), LocalDateTime.now().minusDays(1));
        saveReport("s-new", member.getId(), LocalDateTime.now());
        saveReport("s-other", other.getId(), LocalDateTime.now());

        List<ReportResponseDto> reports = reportQueryService.getMyReports(EMAIL);

        assertThat(reports).hasSize(2);
        assertThat(reports.get(0).sessionId()).isEqualTo("s-new");
        assertThat(reports.get(1).sessionId()).isEqualTo("s-old");
    }

    @Test
    void 단건_상세조회시_emotionScores가_Map으로_역직렬화된다() {
        InterviewReport saved = saveReport("s-detail", member.getId(), LocalDateTime.now());

        ReportResponseDto dto = reportQueryService.getMyReport(EMAIL, saved.getId());

        assertThat(dto.dominantEmotion()).isEqualTo(Emotion.TENSION);
        assertThat(dto.emotionScores()).containsEntry("tension", 0.62);
        assertThat(dto.emotionScores()).containsEntry("calm", 0.38);
    }

    @Test
    void 타인_리포트_단건조회는_REPORT_FORBIDDEN() {
        InterviewReport othersReport = saveReport("s-other-detail", other.getId(), LocalDateTime.now());

        assertThatThrownBy(() -> reportQueryService.getMyReport(EMAIL, othersReport.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REPORT_FORBIDDEN);
    }

    @Test
    void 존재하지_않는_리포트_단건조회는_REPORT_NOT_FOUND() {
        assertThatThrownBy(() -> reportQueryService.getMyReport(EMAIL, 99999999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REPORT_NOT_FOUND);
    }
}
