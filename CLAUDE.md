# CLAUDE.md

AI 기반 자기분석 가상 면접 솔루션 **'결(結)'** 의 백엔드(BFF/Main Server) 레포지토리.
전체 기획 및 개발 로드맵은 `src/main/resources/plan.md` 참고 — 작업 전 반드시 읽을 것.
FastAPI(AI 서버)와의 연동 규약은 `docs/integration-spec.md` 참고.

## 프로젝트 개요

- MSA 기반 BFF 이중 서버 아키텍처: **Spring Boot(이 레포, 회원/인증/리포트 적재)** + FastAPI(AI 스트리밍 서버, 별도 레포)
- 기술 스택: Spring Boot 3.5.x (Java 17), Spring Security, Spring Data JPA, MySQL, Redis, Kafka, JJWT 0.12.x
- 인증 설계 원칙: **하나의 이메일 = 하나의 계정** (Form 가입과 소셜 로그인을 이메일 기준으로 병합)

## 빌드 / 실행

```bash
./gradlew build          # 빌드 + 테스트
./gradlew test           # 테스트만
./gradlew bootRun        # 로컬 실행

cp .env.example .env     # 최초 1회, 실제 값 입력
docker compose up -d     # MySQL(3306), Redis(6379) 기동
```

- 환경변수(시크릿)는 `.env`로 관리하며 **절대 커밋하지 않는다.** 새 환경변수 추가 시 `.env.example`에 템플릿을 함께 추가할 것.
- `application.yml`에 시크릿 원문을 하드코딩하지 말고 `${ENV_VAR}` 참조로 작성할 것.

## 패키지 구조

`io.jieum.gyulbackendrepository` 하위:

```
domain/                     # 도메인별 수직 분할
  auth/
    form/                   # Form 인증 (controller / service)
    oauth2/                 # 소셜 로그인 (handler / service) — 예정
    model/dto/              # 인증 관련 DTO (record 사용)
  user/                     # 회원 (controller / service / model/entity / repository)
global/
  config/                   # SecurityConfig, RedisConfig 등
  jwt/                      # JwtProvider, JwtAuthenticationFilter, JwtProperties
```

- 새 기능은 `domain/<도메인>/` 아래에 controller / service / model / repository 계층으로 추가한다.
- 횡단 관심사(설정, JWT, 공통 예외 등)는 `global/`에 둔다.
- DTO는 Java `record` + `jakarta.validation` 어노테이션으로 작성한다.

## 브랜치 전략 (중요)

```
main ← dev ← feat/[#이슈번호] | bug/[#이슈번호]
```

| 브랜치 | 역할 | 규칙 |
|---|---|---|
| `main` | 운영(프로덕션) | **단위 기능이 완성되어 실제 운영 가능할 때에만** `dev`에서 merge |
| `dev` | 개발 통합 | 기능/버그 브랜치의 merge 대상. 테스트 통과 후에만 merge |
| `feat/[#이슈번호]` | 기능 개발 | GitHub 이슈 번호 기준으로 `dev`에서 생성 (예: `feat/#4`) |
| `bug/[#이슈번호]` | 버그 수정 | 버그용 이슈를 발급받아 `dev`에서 생성 (예: `bug/#2`) |

> ⚠️ 기능 브랜치에 `dev/` 접두사를 쓰지 않는다. `dev` 브랜치가 이미 존재하면 Git ref 구조상
> `dev/feat/...` 브랜치를 만들 수 없다(`cannot lock ref ... 'dev' exists`).

### 작업 흐름

1. 기능(또는 버그) 단위로 GitHub **이슈를 먼저 발급**한다.
2. `dev` 브랜치에서 `feat/[#이슈번호]`(기능) 또는 `bug/[#이슈번호]`(버그 수정) 브랜치를 생성한다.
3. 기능 브랜치에 커밋/푸시하며 개발한다.
4. **테스트 완료 후** `dev` 브랜치에 merge한다 (PR 권장).
5. 단위 기능이 완성되어 실제 운영이 가능한 상태일 때에만 `dev` → `main` merge를 수행한다.
6. `main`에 직접 커밋/푸시하지 않는다.
7. 이슈 자동 종료(`closes #N`)는 base가 `main`(기본 브랜치)인 PR에서만 동작한다. `dev`로 머지하는
   PR로는 닫히지 않으므로, 기능/버그 이슈는 **수동으로 close**한다.

## 커밋 컨벤션

기존 히스토리 형식을 따른다:

```
[#이슈번호] 한글로 작업 내용 요약
```

예: `[#1] 로그인 서비스로직 구현`

- 하나의 커밋은 하나의 논리적 변경만 담는다 (히스토리 참고: 의존성 추가, 엔티티 정의, 서비스 구현을 각각 분리 커밋).

## API / 보안 규칙

- 인증 불필요(permitAll) 경로: `/api/v1/signup`, `/auth/signin`, `/auth/reissue`, `/oauth2/**`, `/login/oauth2/**` — 그 외 모든 요청은 JWT 인증 필수.
- 새 공개 엔드포인트 추가 시 `SecurityConfig`의 permitAll 목록도 함께 갱신할 것.
- 세션은 STATELESS. 상태는 Redis로 관리한다 (Refresh Token 키: `RT:{email}`, TTL은 `jwt.refresh-expiration`과 동일하게 유지).
- Refresh Token 불일치(탈취 의심) 시 해당 키를 삭제해 전체 세션을 무효화하는 기존 정책을 유지한다.
- 비밀번호는 BCrypt로만 저장한다.

## 진행 규칙 (plan.md Ground Rule 요약)

1. **API 명세 우선 설계**: 프론트/Spring/FastAPI 간 통신 규약(JSON, WebSocket Frame)을 먼저 협의한 뒤 구현한다.
2. **로컬 인프라 통일**: `docker compose up`으로 전체 인프라를 띄워 개발한다.
3. FastAPI와의 연동은 JWT 교차 검증(FastAPI가 Spring 발급 토큰 검증), 분석 리포트는 Kafka 비동기 발행으로 수신한다.