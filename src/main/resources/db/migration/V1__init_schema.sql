-- ──────────────────────────────────────
-- V1: 초기 스키마 (결(結) 백엔드)
-- 엔티티: Member(MEMBER), InterviewReport(INTERVIEW_REPORT)
-- 이후 스키마 변경은 V2, V3... 마이그레이션으로 추가한다.
-- ──────────────────────────────────────

CREATE TABLE IF NOT EXISTS member (
    member_id   BIGINT       NOT NULL AUTO_INCREMENT,
    email       VARCHAR(255),
    password    VARCHAR(255),
    name        VARCHAR(255),
    created_at  DATETIME(6),
    gender      VARCHAR(255),
    role        VARCHAR(255),
    provider    VARCHAR(255),
    provider_id VARCHAR(255),
    PRIMARY KEY (member_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS interview_report (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    session_id       VARCHAR(255) NOT NULL,
    member_id        BIGINT       NOT NULL,
    phase            VARCHAR(255),
    dominant_emotion VARCHAR(255),
    emotion_scores   TEXT,
    summary          TEXT,
    started_at       DATETIME(6),
    ended_at         DATETIME(6),
    created_at       DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_interview_report_session_id UNIQUE (session_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
