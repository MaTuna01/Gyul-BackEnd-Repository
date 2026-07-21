-- ──────────────────────────────────────
-- 결(結) 초기 DB 설정
-- 컨테이너 최초 생성 시 1회만 실행됩니다.
-- ──────────────────────────────────────

-- 문자셋 확인
ALTER DATABASE gyul_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- MEMBER 테이블 (JPA ddl-auto: update 사용 시 자동 생성되지만, 명시적 스키마 참고용)
-- Hibernate가 자동으로 생성하므로 아래는 참고용 주석입니다.
-- 실제 테이블 생성은 Spring Boot 기동 시 JPA가 수행합니다.

/*
CREATE TABLE IF NOT EXISTS MEMBER (
    member_id   BIGINT          NOT NULL AUTO_INCREMENT,
    email       VARCHAR(255),
    password    VARCHAR(255),
    name        VARCHAR(255),
    created_at  DATETIME(6),
    gender      VARCHAR(20),
    role        VARCHAR(20),
    provider    VARCHAR(50),
    provider_id VARCHAR(255),
    PRIMARY KEY (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
*/
