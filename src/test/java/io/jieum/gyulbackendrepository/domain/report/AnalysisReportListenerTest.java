package io.jieum.gyulbackendrepository.domain.report;

import io.jieum.gyulbackendrepository.domain.report.messaging.AnalysisReportListener;
import io.jieum.gyulbackendrepository.domain.report.messaging.ReportMessageParseException;
import io.jieum.gyulbackendrepository.domain.report.model.entity.Emotion;
import io.jieum.gyulbackendrepository.domain.report.model.entity.InterviewReport;
import io.jieum.gyulbackendrepository.domain.report.repository.InterviewReportRepository;
import io.jieum.gyulbackendrepository.domain.user.model.entity.Member;
import io.jieum.gyulbackendrepository.domain.user.model.entity.Role;
import io.jieum.gyulbackendrepository.domain.user.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class AnalysisReportListenerTest {

    @Autowired
    private AnalysisReportListener listener;
    @Autowired
    private InterviewReportRepository reportRepository;
    @Autowired
    private MemberRepository memberRepository;

    private static final String EMAIL = "report-test@example.com";
    private static final String SESSION_ID = "conv-test-0001";

    private Member member;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
        memberRepository.findByEmail(EMAIL).ifPresent(memberRepository::delete);
        member = memberRepository.save(Member.builder()
                .email(EMAIL)
                .password("x")
                .name("리포트테스트")
                .createdAt(LocalDateTime.now())
                .role(Role.MEMBER)
                .build());
    }

    @AfterEach
    void tearDown() {
        reportRepository.deleteAll();
        memberRepository.findByEmail(EMAIL).ifPresent(memberRepository::delete);
    }

    private String message(String sessionId, String email) {
        return """
                {
                  "schemaVersion": 1,
                  "sessionId": "%s",
                  "email": "%s",
                  "phase": "PHASE_1",
                  "startedAt": "2026-07-21T10:00:00Z",
                  "endedAt": "2026-07-21T10:12:34Z",
                  "emotion": { "dominant": "TENSION", "scores": { "tension": 0.62, "calm": 0.38 } },
                  "summary": "테스트 요약",
                  "metrics": { "turnCount": 18 }
                }
                """.formatted(sessionId, email);
    }

    @Test
    void 정상_리포트를_적재한다() {
        listener.onMessage(message(SESSION_ID, EMAIL));

        Optional<InterviewReport> saved = reportRepository.findAll().stream().findFirst();
        assertThat(saved).isPresent();
        assertThat(saved.get().getSessionId()).isEqualTo(SESSION_ID);
        assertThat(saved.get().getMemberId()).isEqualTo(member.getId());
        assertThat(saved.get().getDominantEmotion()).isEqualTo(Emotion.TENSION);
        assertThat(saved.get().getPhase()).isEqualTo("PHASE_1");
        assertThat(saved.get().getEmotionScores()).contains("tension");
        assertThat(saved.get().getStartedAt()).isNotNull();
    }

    @Test
    void 동일_sessionId는_멱등하게_한번만_적재된다() {
        listener.onMessage(message(SESSION_ID, EMAIL));
        listener.onMessage(message(SESSION_ID, EMAIL));

        assertThat(reportRepository.count()).isEqualTo(1);
    }

    @Test
    void 미존재_이메일_리포트는_스킵된다() {
        listener.onMessage(message("conv-unknown", "nobody@example.com"));

        assertThat(reportRepository.count()).isZero();
    }

    @Test
    void 잘못된_JSON은_파싱예외를_던져_DLQ로_라우팅된다() {
        // 파싱 실패는 예외를 전파해 에러 핸들러가 DLQ로 보낸다 (적재는 되지 않음)
        assertThatThrownBy(() -> listener.onMessage("{ not valid json"))
                .isInstanceOf(ReportMessageParseException.class);

        assertThat(reportRepository.count()).isZero();
    }
}
