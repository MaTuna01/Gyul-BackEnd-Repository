# 🚀 AI 기반 자기분석 가상 면접 솔루션 '결(結)' - 백엔드 개발 로드맵

## 1. 프로젝트 개요 및 아키텍처
본 프로젝트는 구직자의 무의식적인 음성/비전 데이터를 분석하여 진짜 성향을 파악하고, 이를 바탕으로 실전 면접 훈련을 제공하는 AI 솔루션입니다. 트래픽 분산과 실시간 오디오 스트리밍 최적화를 위해 **MSA 기반의 BFF(Backend for Frontend) 이중 서버 아키텍처**를 채택합니다.

### 🛠️ 핵심 기술 스택
* **BFF / Main Server:** Spring Boot 3.x, Spring Security, Spring Data JPA, MySQL
* **AI Streaming Server:** FastAPI, Python, WebSockets, LangChain / LangGraph
* **Infra & State Management:** Docker Compose, Redis, Kafka, Nginx
* **AI Pipeline:** OpenAI Whisper(STT), GPT-4o-mini(LLM), ChromaDB(RAG), OpenCV(Vision)

---

## 2. 페이즈별 개발 목표 (Phase 1 & 2)

### 🎯 Phase 1: 1학기 MVP (자기분석 대화 시나리오)
**목표:** RAG 배제, 사용자 인증 및 LangChain 기반의 통제된 일상 대화 파이프라인 구축

* **Spring Boot (회원 관리 및 인가)**
    * [x] 일반 Form 회원가입 / 로그인 API 구현
    * [x] JJWT 기반 Access/Refresh Token 발급 및 Redis 세션 관리 로직 구현
    * [x] SecurityConfig, JwtProvider, JwtAuthenticationFilter 구현
    * [x] OAuth 2.0 (Google, Kakao) 소셜 로그인 연동 (이슈 #6)
    * [x] 소셜 로그인 최초 가입 시 추가정보(성별) 입력 플로우 구현 (이슈 #6, `PUT /api/v1/members/me/profile`)
* **FastAPI (실시간 대화 및 감정 분석)**
    * [ ] WebSocket 엔드포인트 개통 및 Spring Boot 발급 JWT 교차 검증 로직 구현
    * [ ] LangChain + RedisChatMessageHistory를 활용한 대화 세션(Memory) 유지
    * [ ] STT(Whisper) -> LangChain -> TTS 파이프라인 연결
    * [ ] 사용자 음성 데이터 기반 감정(긴장, 당황 등) 추출 모델 연동 (Vision 제외)
* **시스템 연동**
    * [~] 대화 종료 시 FastAPI -> Kafka -> Spring Boot 방향으로 최종 분석 리포트 비동기 발행 및 적재
        * [x] Spring 측 Kafka Consumer 및 적재 (sessionId 멱등, 미존재 이메일 스킵) (이슈 #10)
        * [ ] FastAPI 측 리포트 발행 (별도 레포)

### 📊 백엔드(Spring Boot) 진행 현황 (2026-07-22 기준)

Phase 1의 **Spring Boot 측 작업은 기능적으로 완료** 상태이며, 남은 Phase 1 항목은 대부분 FastAPI(별도 레포) 몫이다.

| 이슈 | 작업 | 상태 |
|---|---|---|
| #1 | Form 회원가입/로그인, JWT(Access/Refresh) + Redis 세션, SecurityConfig/JwtProvider/Filter | ✅ dev 머지 |
| #2 | 회원가입 시 기본 role(MEMBER) 누락 수정 | ✅ dev 머지 |
| #4 | Spring ↔ FastAPI 연동 명세서 작성 (`docs/integration-spec.md`) | ✅ dev 머지 |
| #6 | OAuth 2.0(Google/Kakao) 소셜 로그인 + 추가정보(성별) 입력 API | ✅ dev 머지 |
| #8 | Redis DB 인덱스 분리 (Refresh Token을 DB 1로) | ✅ dev 머지 |
| #10 | Kafka 분석 리포트 Consumer 및 적재 (sessionId 멱등) | ✅ dev 머지 |
| #12 | 분석 리포트 조회 API (`GET /api/v1/members/me/reports`) | ✅ dev 머지 |
| #14 | 전역 예외 처리 및 표준 에러 응답 규약 (`global/exception/`) | ✅ dev 머지 |
| #16 | 필터 단계 인증/인가 예외 응답 통일 (401/403 표준화) | ✅ dev 머지 |
| #20 | JWT Access Token에 `memberId` claim 추가 (이메일 변경 대비) | 🔵 feat/#20 (dev 머지 대기) |

**다음 후보 작업**
* [ ] 연동 명세서 §6 백엔드 항목: Kafka DLQ/재처리 정책
* [ ] 테스트/빌드 환경 개선: `.env`의 gradle 테스트 자동 주입, Testcontainers 도입
* [ ] `dev` → `main` 승격 (Phase 1 백엔드 단위 기능 완성 시점)

> ⚠️ 이슈/PR 번호는 GitHub 기준. `feat/[#이슈]` → `dev` PR로 머지하며 이슈는 수동 close한다(브랜치 전략 참고).

### 🚀 Phase 2: 2학기 고도화 (IT 기술 면접 시뮬레이터)
**목표:** RAG 연동 및 비전 기술 융합을 통한 압박 면접 환경 구현

* **AI 파이프라인 고도화 (FastAPI)**
    * [ ] **RAG 도입:** ChromaDB를 활용하여 사용자 이력서, 기업 인재상, 기술 면접 기출문제 벡터 임베딩 및 검색 체인(Retrieval Chain) 구축
    * [ ] **LangGraph 도입:** 상태 머신(State Machine) 기반의 에이전트를 구축하여, 면접 단계(도입 -> 기술 질문 -> 꼬리 질문 -> 마무리)를 완벽하게 제어
    * [ ] **비전 융합:** OpenCV를 활용하여 클라이언트 웹캠 데이터(시선 회피, 표정 등)를 실시간 분석하고 감정 데이터에 가중치 부여
* **성능 최적화 및 안정화**
    * [ ] VAD(Voice Activity Detection) 튜닝 및 Faster-Whisper 도입으로 STT 지연 시간 최소화
    * [ ] LLM 응답 스트리밍(Chunking) 및 백채널링(추임새) 비동기 처리를 통한 실시간 UX 확보

---

## 3. 인증/인가 아키텍처 상세 설계

### 📌 설계 원칙
* **하나의 이메일 = 하나의 계정** (이메일 기준 계정 병합)
* Form 가입과 소셜 로그인 사용자를 이메일 기준으로 통합 관리
* 소셜 로그인 최초 가입 시 추가정보(성별) 입력 필수

### 🔐 Form 인증 (구현 완료)

| 항목 | 상세 |
|---|---|
| 회원가입 | `POST /api/v1/signup` — BCrypt 암호화, 이메일 중복 검사 |
| 로그인 | `POST /auth/signin` — 비밀번호 검증 후 JWT 발급 |
| 토큰 재발급 | `POST /auth/reissue` — Refresh Token 유효성 + Redis 대조 후 토큰 쌍 재발급 |
| 로그아웃 | `POST /auth/signout` — Redis에서 Refresh Token 삭제 |

### 🌐 OAuth 2.0 소셜 로그인 (Google, Kakao)

#### 인증 흐름

```
[프론트엔드] → GET /oauth2/authorization/{provider}
    → [Google/Kakao 로그인 페이지]
    → 사용자 인증 완료
    → GET /login/oauth2/code/{provider}?code=xxx
    → CustomOAuth2UserService.loadUser()
        → findByEmail()로 기존 회원 조회
        → 기존 회원 있음 → provider/providerId 연결 (계정 병합)
        → 기존 회원 없음 → 신규 Member 생성 (gender=null)
    → OAuth2LoginSuccessHandler
        → JWT Access/Refresh Token 생성
        → Refresh Token → Redis 저장
        → gender == null → 추가정보 입력 페이지로 리다이렉트
        → gender != null → 메인 페이지로 리다이렉트
```

#### 계정 병합 전략
* 동일 이메일로 Form 가입 후, 같은 이메일의 소셜 계정으로 로그인 시도 시 기존 계정에 `provider`, `providerId`를 업데이트하여 연결
* 이후 Form 로그인과 소셜 로그인 모두 같은 계정으로 접근 가능

#### 추가정보 입력 (소셜 로그인 최초 가입)
* 소셜 로그인 최초 가입 시 `gender`가 `null` 상태로 Member 생성
* SuccessHandler에서 `gender == null` 감지 시 프론트엔드의 추가정보 입력 페이지로 리다이렉트
* `PUT /api/v1/members/me/profile` API를 통해 성별 정보 업데이트 (JWT 인증 필요)

#### 구현 대상 파일

| 구분 | 파일 | 설명 |
|---|---|---|
| **[NEW]** | `CustomOAuth2UserService.java` | 소셜 로그인 후처리 — 이메일 기준 계정 병합 로직 |
| **[NEW]** | `OAuth2LoginSuccessHandler.java` | 로그인 성공 시 JWT 발급 + 추가정보 입력 분기 리다이렉트 |
| **[NEW]** | `OAuth2LoginFailureHandler.java` | 로그인 실패 시 에러 처리 |
| **[NEW]** | `MemberProfileController.java` | `PUT /api/v1/members/me/profile` 추가정보 입력 API |
| **[NEW]** | `MemberProfileService.java` | 프로필(성별) 업데이트 서비스 로직 |
| **[NEW]** | `ProfileUpdateRequestDto.java` | 추가정보 입력 요청 DTO |
| **[MODIFY]** | `Member.java` | `provider`, `providerId` 필드 + `updateGender()` 메서드 추가 |
| **[MODIFY]** | `MemberRepository.java` | `findByProviderAndProviderId()` 쿼리 메서드 추가 |
| **[MODIFY]** | `SecurityConfig.java` | `.oauth2Login()` 설정, 핸들러 주입 |
| **[MODIFY]** | `application.yml` | Naver 제거, Kakao provider 설정 추가 |

#### 패키지 구조

```
src/main/java/io/jieum/gyulbackendrepository/
├── domain/
│   ├── auth/
│   │   ├── form/                              # Form 인증 (구현 완료)
│   │   │   ├── controller/
│   │   │   │   ├── SignInController.java
│   │   │   │   └── SignUpController.java
│   │   │   └── service/
│   │   │       ├── SignInService.java
│   │   │       └── SignUpService.java
│   │   ├── oauth2/                            # [NEW] 소셜 로그인
│   │   │   ├── handler/
│   │   │   │   ├── OAuth2LoginSuccessHandler.java
│   │   │   │   └── OAuth2LoginFailureHandler.java
│   │   │   └── service/
│   │   │       └── CustomOAuth2UserService.java
│   │   └── model/dto/
│   │       ├── SignInRequestDto.java
│   │       ├── SignUpRequestDto.java
│   │       ├── ReissueRequestDto.java
│   │       ├── TokenResponseDto.java
│   │       └── ProfileUpdateRequestDto.java   # [NEW]
│   └── user/
│       ├── controller/
│       │   └── MemberProfileController.java   # [NEW]
│       ├── service/
│       │   └── MemberProfileService.java      # [NEW]
│       ├── model/entity/
│       │   ├── Member.java                    # [MODIFY]
│       │   ├── Gender.java
│       │   └── Role.java
│       └── repository/
│           └── MemberRepository.java          # [MODIFY]
└── global/
    ├── config/
    │   ├── SecurityConfig.java                # [MODIFY]
    │   └── RedisConfig.java
    └── jwt/
        ├── JwtProvider.java
        ├── JwtAuthenticationFilter.java
        └── JwtProperties.java
```

#### application.yml 소셜 로그인 설정 (Kakao는 수동 provider 등록 필요)

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: email, profile
          kakao:
            client-id: ${KAKAO_CLIENT_ID}
            client-secret: ${KAKAO_CLIENT_SECRET}
            client-name: Kakao
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            client-authentication-method: client_secret_post
            scope: account_email, profile_nickname
        provider:
          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id

app:
  oauth2:
    redirect-uri: http://localhost:3000  # 프론트 개발 완료 후 실제 주소로 교체
```

#### 사전 준비 사항
* **Google Cloud Console:** OAuth 2.0 클라이언트 ID 생성, Redirect URI 등록 (`http://localhost:8080/login/oauth2/code/google`)
* **Kakao Developers:** 앱 생성 → REST API 키 발급, 카카오 로그인 활성화, Redirect URI 등록 (`http://localhost:8080/login/oauth2/code/kakao`), **동의항목에서 이메일 수집 '필수' 설정**

---

## 4. 인프라 및 배포 전략 (Deployment)
비용 효율성을 극대화하기 위해 단일 고성능 인스턴스 내에서 컨테이너 기반 오케스트레이션을 진행합니다.

* **환경:** AWS EC2 g5.xlarge (GPU 인스턴스)
* **구조:** `docker-compose.yml`을 활용하여 Spring Boot, FastAPI, MySQL, Redis, Kafka를 kubernetes를 통해 배포
* **보안:** 가비아/Route53 도메인 연결 및 Nginx 리버스 프록시를 통한 SSL/HTTPS 암호화 적용 (Web Audio API 사용 필수 조건)

---

## 5. 진행 규칙 (Ground Rule)
1.  **API 명세 우선 설계:** 프론트엔드, Spring Boot, FastAPI 간의 통신 규약(JSON, WebSocket Frame)을 선행하여 협의합니다.
2.  **독립적 개발:** 로컬 환경에서 `docker-compose up` 명령어로 전체 인프라를 통일하여 개발 생산성을 높입니다.
3.  **지표 기반 평가 (QA):** RAGAS 프레임워크(할루시네이션 검증) 및 KEMDy20 데이터셋(감정 F1-Score)을 활용하여 AI 파이프라인의 성능을 객관적으로 수치화합니다.