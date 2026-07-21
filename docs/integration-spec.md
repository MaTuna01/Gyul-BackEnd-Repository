# Spring Boot ↔ FastAPI 연동 명세서

> '결(結)' 프로젝트의 **BFF/Main Server(Spring Boot)** 와 **AI Streaming Server(FastAPI)** 간
> 통신 규약. 두 서버는 별도 GitHub 레포로 개발되며, 이 문서가 **유일한 계약(contract)** 이다.
> 규약 변경 시 이 문서를 먼저 갱신하고 양쪽 팀이 합의한다. (plan.md Ground Rule #1)

- 상태: **초안(Draft)** — FastAPI 팀 리뷰 필요
- 관련 이슈: #4
- 최종 갱신 근거 커밋: `feat/#4`

---

## 0. 역할 분담

| 서버 | 책임 |
|---|---|
| **Spring Boot** | 회원가입/로그인, JWT 발급, Refresh Token 관리, **분석 리포트 적재/조회** |
| **FastAPI** | WebSocket 실시간 대화, STT(Whisper), LangChain 대화세션, 감정 분석, **분석 리포트 발행** |

두 서버의 접점은 아래 **3가지**뿐이다.

1. **JWT 교차 검증** — FastAPI가 Spring이 발급한 토큰을 검증 (인증 위임)
2. **WebSocket 인증** — 클라이언트가 Spring 발급 토큰으로 FastAPI WS에 접속
3. **Kafka 리포트 전달** — 대화 종료 시 FastAPI → Spring (비동기 적재)

```
                 ┌────────── (1) POST /auth/signin ──────────┐
   [Client] ─────┤                                            ▼
      │          │                                     [Spring Boot]
      │          └──── JWT(Access/Refresh) ◀───────────────┘
      │
      │  (2) WS 연결 + Access Token
      ▼
 [FastAPI] ── JWT 서명 검증(공유 시크릿) ── 대화/STT/감정분석
      │
      │  (3) 대화 종료 → 분석 리포트
      ▼
   [Kafka] ──── topic: interview.analysis-report ────▶ [Spring Boot] → DB 적재
```

---

## 1. JWT 교차 검증

### 1.1 서명 방식 (현재 확정)

Spring은 현재 **HS256(대칭키)** 로 서명한다. FastAPI는 **동일한 시크릿**으로 서명을 검증한다.

| 항목 | 값 |
|---|---|
| 알고리즘 | `HS256` |
| 시크릿 | 환경변수 `JWT_SECRET` (양 서버 동일 값 주입, 32바이트 이상) |
| 검증 항목 | 서명 유효성 + `exp` 만료 여부 |

> **시크릿 공유가 곧 신뢰의 기반**이다. `.env`로만 주입하며 레포에 커밋 금지.
> 배포 시 두 컨테이너에 같은 `JWT_SECRET`을 환경변수로 넣는다 (docker-compose/K8s Secret).

**Access Token Claims** (Spring `JwtProvider.generateAccessToken()` 기준)

| Claim | 타입 | 설명 |
|---|---|---|
| `sub` | string | **사용자 이메일** (사용자 식별자) |
| `role` | string | 권한 (`MEMBER` / `ADMIN`) |
| `iat` | number | 발급 시각 |
| `exp` | number | 만료 시각 (Access 30분) |

> FastAPI는 사용자를 **`sub`(이메일)** 로 식별한다. 별도 memberId claim은 현재 없음
> — 필요 시 Spring이 `memberId` claim 추가를 검토(§5 변경 관리).

**FastAPI 검증 예시 (PyJWT)**

```python
import jwt  # PyJWT
payload = jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
email = payload["sub"]          # 사용자 식별
role  = payload.get("role")
```

### 1.2 향후 강화(선택)

멀티 서비스 보안을 높이려면 **RS256(비대칭)** 으로 전환 검토 가능:
Spring이 private key로 서명, FastAPI는 public key(JWKS)로 검증 → 시크릿 공유 불필요.
**MVP 단계에서는 HS256 공유 시크릿을 유지**한다.

---

## 2. WebSocket 인증

### 2.1 연결 규약

브라우저 WebSocket은 커스텀 헤더를 넣기 어려우므로, Access Token은 **쿼리 파라미터** 또는
**Sec-WebSocket-Protocol** 로 전달한다. **기본안: 쿼리 파라미터.**

```
wss://<ai-server-host>/ws/interview?token=<ACCESS_TOKEN>
```

### 2.2 처리 순서 (FastAPI)

1. 핸드셰이크 시 `token` 추출 → §1.1 방식으로 서명/만료 검증
2. 실패 시 **연결 거부** (close code `4401` = 인증 실패로 약속)
3. 성공 시 `sub`(이메일)를 세션에 바인딩하고 대화 시작

| close code | 의미 |
|---|---|
| `4401` | 토큰 없음/유효하지 않음/만료 |
| `4403` | 권한 없음 |
| `1000` | 정상 종료 |

> Access Token 만료(30분) 시 클라이언트는 Spring `POST /auth/reissue`로 재발급 후 재연결한다.
> WebSocket 세션 중 만료 처리 정책(강제 종료 vs 유예)은 **협의 필요(TODO)**.

---

## 3. Kafka 분석 리포트 (FastAPI → Spring)

대화 종료 시 FastAPI가 최종 분석 리포트를 발행하고, Spring이 소비하여 DB에 적재한다.

| 항목 | 값 |
|---|---|
| 토픽명 | `interview.analysis-report` |
| Producer | FastAPI |
| Consumer | Spring Boot (`spring-kafka`, 컨슈머 그룹 `gyul-backend`) |
| 직렬화 | JSON (UTF-8) |
| Key | `email` (파티셔닝/순서 보장 기준) |

### 3.1 메시지 스키마 (v1)

```json
{
  "schemaVersion": 1,
  "sessionId": "conv-20260721-abc123",
  "email": "user@example.com",
  "phase": "PHASE_1",
  "startedAt": "2026-07-21T10:00:00Z",
  "endedAt":   "2026-07-21T10:12:34Z",
  "emotion": {
    "dominant": "TENSION",
    "scores": { "tension": 0.62, "confusion": 0.21, "calm": 0.17 }
  },
  "summary": "사용자는 전반적으로 긴장 상태를 보였으며...",
  "metrics": {
    "speechDurationSec": 540,
    "turnCount": 18
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|:---:|---|
| `schemaVersion` | int | ✅ | 스키마 버전 (하위호환 관리) |
| `sessionId` | string | ✅ | 대화 세션 고유 ID (멱등성 키로도 사용) |
| `email` | string | ✅ | 사용자 식별자 (JWT `sub`와 동일) |
| `phase` | string | ✅ | `PHASE_1`(자기분석) / `PHASE_2`(기술면접) |
| `startedAt`/`endedAt` | ISO-8601(UTC) | ✅ | 대화 시작/종료 시각 |
| `emotion.dominant` | string | ✅ | 대표 감정 (enum, §3.2) |
| `emotion.scores` | object | ✅ | 감정별 확률(0~1) |
| `summary` | string | ✅ | LLM 생성 요약 |
| `metrics` | object | ⬜ | 부가 지표 (확장 가능) |

### 3.2 감정 enum (합의값)

`TENSION`(긴장), `CONFUSION`(당황), `CALM`(평온), `CONFIDENCE`(자신감), `NEUTRAL`(중립)
— 신규 값 추가 시 이 문서 갱신 + Spring `Emotion` enum 동기화.

### 3.3 신뢰성/멱등성

- Spring Consumer는 `sessionId`로 **중복 수신을 무시**한다(멱등 처리). Kafka는 at-least-once.
- 존재하지 않는 `email`이면 로그 남기고 스킵(또는 DLQ). 예외로 컨슈머가 멈추지 않게 처리.
- 스키마 파싱 실패 메시지는 **DLQ 토픽** `interview.analysis-report.dlq`로 보낸다(TODO).

### 3.4 Spring 측 적재 스키마 (예정)

`domain/report/` 도메인 신설 예정. 대략:

| 테이블 `INTERVIEW_REPORT` | 타입 | 설명 |
|---|---|---|
| `id` | BIGINT PK | |
| `session_id` | VARCHAR unique | 멱등 키 |
| `member_id` | BIGINT FK → MEMBER | `email`로 조회해 매핑 |
| `phase` | VARCHAR | |
| `dominant_emotion` | VARCHAR | |
| `emotion_scores` | JSON | |
| `summary` | TEXT | |
| `started_at`/`ended_at` | DATETIME | |
| `created_at` | DATETIME | 적재 시각 |

조회 API(예: `GET /api/v1/members/me/reports`)는 별도 이슈로 구현.

---

## 4. Redis 사용 영역 분리

두 서버가 같은 Redis 인스턴스를 쓰므로 **DB 인덱스로 격리**한다 (plan.md 기준).

| DB | 사용 주체 | 용도 | 키 패턴 |
|---|---|---|---|
| `0` | FastAPI | 대화 세션(LangChain memory) | (FastAPI 규약) |
| `1` | Spring | Refresh Token | `RT:{email}` |

> ✅ Spring은 `application.yml`의 `spring.data.redis.database: 1`로 DB 1을 사용한다 (이슈 #8).
> FastAPI는 대화 세션을 **DB 0** 에 저장하도록 맞춘다.

---

## 5. 변경 관리

- 이 문서가 **단일 소스 오브 트루스**. 규약 변경은 PR로 하고 양 팀 리뷰어 승인 필수.
- 메시지 스키마는 `schemaVersion`으로 관리. 필드 **추가는 하위호환**(consumer 무시 가능), **삭제/의미변경은 버전 상향**.
- Enum(감정, phase, close code) 추가 시 이 문서와 양쪽 코드 동시 갱신.

## 6. 미결 항목 (TODO — 협의 필요)

- [ ] WebSocket 세션 중 Access Token 만료 처리 정책 (강제종료 vs 유예)
- [x] Redis DB 인덱스 정렬 (Spring을 DB 1로 이동) — 이슈 #8 완료
- [ ] JWT에 `memberId` claim 추가 여부 (이메일 변경 대비)
- [ ] Kafka DLQ 및 재처리 정책 확정
- [ ] 리포트 조회 API 명세
