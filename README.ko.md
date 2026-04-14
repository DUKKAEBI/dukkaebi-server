# 두깨비(Dukkaebi) Server

> [English](./README.md) · **한국어**

코딩 학습 플랫폼 **두깨비**의 백엔드 서버입니다. 학생은 문제를 풀며 점수를 쌓고 "도깨비 → 신깨비"로 성장하며, 강사는 코스·문제·대회를 만들어 학생을 관리합니다. 이 문서는 신규 합류자 / 인수인계를 위한 상세 안내서입니다.

---

## 목차
1. [서비스 개요](#1-서비스-개요)
2. [기술 스택](#2-기술-스택)
3. [프로젝트 구조](#3-프로젝트-구조)
4. [도메인별 기능 요약](#4-도메인별-기능-요약)
5. [인증 / 권한](#5-인증--권한)
6. [API 라우팅 요약](#6-api-라우팅-요약)
7. [환경 변수 & 설정](#7-환경-변수--설정)
8. [로컬 실행 방법](#8-로컬-실행-방법)
9. [빌드 & 배포 파이프라인](#9-빌드--배포-파이프라인)
10. [채점 시스템 동작 원리](#10-채점-시스템-동작-원리)
11. [외부 연동](#11-외부-연동)
12. [운영 주의사항 / 알려진 이슈](#12-운영-주의사항--알려진-이슈)

---

## 1. 서비스 개요

- **목적**: 코딩 학습용 웹 서비스. 학생은 문제를 풀고(제출·테스트·저장), 챗봇(두비)에게 질문하고, 코스에 참여하고, 대회(Contest)에 참가합니다. 관리자(강사)는 문제·코스·공지·대회·회원을 관리합니다.
- **주요 컨셉**
  - **성장 시스템**: 사용자 점수(score)에 따라 `WISP(도깨비불) → COPPER → IRON → SILVER → GOLD → JADE → GOD(신깨비)` 로 자동 승급 (`GrowthType.fromScore`).
  - **문제 난이도**: `COPPER / IRON / SILVER / GOLD / JADE` — 난이도별로 정답 시 1 / 3 / 5 / 10 / 15점 부여.
  - **코스 / 대회**: 강사가 문제를 묶어 배포. 대회는 참여코드(6자리) 기반, 실시간 순위는 SSE로 브로드캐스트.
- **클라이언트**: 학생용 / 관리자용 웹이 별도 배포되며, 허용 오리진은 `SecurityConfig`에 정의되어 있습니다.

## 2. 기술 스택

| 분류 | 내용 |
|---|---|
| 언어 / 런타임 | **Java 21** (Temurin) |
| 프레임워크 | **Spring Boot 3.5.7** (web, data-jpa, security, data-redis) |
| 빌드 | Gradle (wrapper 포함) |
| DB | **MariaDB** (runtime `mariadb-java-client`, MySQL 드라이버도 함께 포함) |
| 캐시/세션 보조 | **Redis** (`spring-boot-starter-data-redis`) |
| 인증 | **JWT** (`jjwt 0.12.6`) — Access / Refresh 2종 |
| 문서화 | **springdoc-openapi 2.7.0** (Swagger UI: `/swagger-ui/index.html`) |
| 파일 저장 | **AWS S3** (`spring-cloud-starter-aws 2.2.6.RELEASE`) |
| 외부 AI | **Google Gemini 2.0 Flash** (챗봇 + 채점 보조) |
| 복원력 | **Resilience4j** (Gemini 호출 RateLimiter + Retry) |
| HTTP 클라이언트 | Apache HttpClient5 + RestTemplate |
| 컨테이너 | Docker (base: `eclipse-temurin:21`, python3 + g++ 포함) |
| CI/CD | GitHub Actions → DockerHub → EC2 SSH 배포 |
| 실시간 | Spring MVC SSE (대회 순위 실시간 전송, 비동기 타임아웃 30분) |

## 3. 프로젝트 구조

도메인 주도 패키지 구성이며, 각 도메인은 `presentation / usecase / service / domain / error` 레이어로 나뉩니다.

```
src/main/java/com/ducami/dukkaebi
├── DukkaebiApplication.java           # 엔트리포인트
├── domain/
│   ├── auth/        # 회원가입, 로그인, 토큰 재발급
│   ├── user/        # 사용자 정보, 활동(streak/contributions), 관리자 회원관리
│   ├── problem/     # 문제 CRUD, 테스트케이스, 제출 히스토리
│   ├── grading/     # 코드 채점(JudgeService) / 테스트 / 저장코드(SavedCode)
│   ├── course/      # 코스 CRUD, 수강/진행상황
│   ├── contest/     # 대회 CRUD, 참가, 제출, 실시간 순위(SSE)
│   ├── chatbot/     # 두비 챗봇 (Gemini 래퍼 + 캐시)
│   └── notice/      # 공지사항
└── global/
    ├── common/      # 공통 응답(Response/ResponseData/PageResponse), S3Service
    ├── config/      # SpringConfig, SwaggerConfig, S3Config, RestTemplateConfig
    ├── exception/   # CustomException, ErrorCode, GlobalExceptionHandler
    └── security/
        ├── SecurityConfig.java
        ├── auth/    # AuthDetails, UserSessionHolder (현재 로그인 사용자)
        └── jwt/     # JwtFilter, JwtProvider, JwtExtractor, JwtTokenService 등
```

### 레이어 규약
- **presentation/controller**: `@RestController`. 요청을 받아 `UseCase`로 위임.
- **usecase**: 트랜잭션 경계. 여러 service/repo를 조합하여 API 한 건의 유스케이스를 완성.
- **service**: 단일 책임 단위의 비즈니스 로직(예: `JudgeService`, `UserActivityService`, `GeminiService`).
- **domain**: JPA 엔티티(`*.java`), `enums/`, `repo/` (JpaRepository 인터페이스).
- **error**: 도메인별 `ErrorCode` (공통 `ErrorCode` 인터페이스 구현).

> **신규 API 추가 시**: `Controller → UseCase → Service → Repo` 순서로 뼈대를 만들고, 요청/응답은 `presentation/dto/request`, `presentation/dto/response`에 record로 정의하는 것이 기존 관례입니다.

## 4. 도메인별 기능 요약

### auth
회원가입(`/auth/sign-up`), 로그인(`/auth/sign-in`), 리프레시(`/auth/refresh`). 로그인 성공 시 Access / Refresh 토큰을 발급합니다. Refresh 토큰은 Redis에 저장되어 재발급 / 로그아웃 시 블랙리스트 처리됩니다.

### user
- `/user` 본인 정보, `/user/list`, `/user/info/{userId}`, `/user/list/filter` (정렬/필터).
- `/user/activity/contributions`, `/user/activity/streak` — GitHub 잔디 스타일의 일일 활동 / 연속 출석.
- `/user/logout`, `/user/delete`.
- `/admin/user/delete/{userId}` — 관리자 회원 삭제.
- 성장(GrowthType)은 점수 변동 시 `User.addScore()` 내부에서 자동 갱신됩니다.

### problem
- 일반 문제 CRUD (`/admin/problems`), 일반 조회 (`/problems`, `/problems/filter`, `/problems/{id}`).
- 엔티티: `Problem`, `ProblemTestCase`, `ProblemHistory` (사용자별 SOLVED / FAILED / NOT_SOLVED).
- `Problem.contestId`가 null이 아니면 "대회 문제"로 취급되어 일반 점수는 부여되지 않고 대회 점수로만 집계됩니다.

### grading (`/solve`)
코드 실행 엔진. 아래 [채점 시스템 동작 원리](#10-채점-시스템-동작-원리) 참고.
- `POST /solve/grading` — 실제 제출 (점수/히스토리/통계 반영)
- `POST /solve/test` — 테스트만 (부수효과 없음)
- `POST /solve/save`, `GET /solve/saved/{problemId}` — 작성 중인 코드 저장/복원.

### course
- 강사(`/admin/course`): 코스 생성/수정/삭제, 코스 ↔ 문제 매핑 추가/삭제.
- 공통(`/course`): 코스 목록/상세/검색.
- 학생(`/student/course`): 코스 참여, 진행 중 / 완료 / 참여 가능 코스 조회.
- `CourseProgressService`가 사용자별 진행률을 계산.

### contest
참여 코드 기반 대회. 6자리 랜덤 코드는 `CodeGenerator`가 생성합니다.
- `/admin/contest`: 대회 생성/수정/종료, 문제 추가(기존 문제 import or 대회 전용 문제 생성), 개별 문제 배점/순서/삭제, 참여자 조회, 수동 점수 보정, 개인 제출물 조회.
- `/contest`: 대회 목록/상세/검색.
- `/student/contest/{code}/join`: 참여.
- `/contest/{code}/subscribe` (SSE): 실시간 스코어보드. 제출이 집계될 때마다 `ContestSseService`가 `ContestUpdateEvent`를 브로드캐스트합니다. `application.yml`의 `spring.mvc.async.request-timeout: 1800000` (30분)이 SSE 타임아웃입니다.
- 채점 시 `Problem.contestId`가 존재하면 `JudgeService`가 `ContestParticipant.totalScore / totalTime`을 재계산하고 `ContestSubmission`(최신 제출 코드)을 갱신합니다.

### chatbot (`/chatbot/chat`)
- `GeminiService`가 Gemini API(`gemini-2.0-flash:generateContent`)를 호출.
- Resilience4j: 초당 3회 RateLimiter + 최대 3회 Retry(지수 백오프, `HttpClientErrorException.TooManyRequests`).
- 동일 질문 중복 호출 방지를 위한 **5초 TTL 메모리 캐시**.
- 시스템 프롬프트: 챗봇 이름 "두비", 존댓말 강제, 코딩 외 질문 거절 등.

### notice
- `/notice`, `/notice/home`, `/notice/{id}`, `/notice/search` — 조회.
- `/admin/notice/create|update|delete`, `/admin/notice/upload-file` — 관리자 전용. multipart 업로드 → S3 저장.

## 5. 인증 / 권한

- `JwtFilter`가 `Authorization: Bearer <token>` 헤더에서 Access 토큰을 파싱해 `SecurityContext`에 올립니다.
- 현재 로그인 사용자는 `UserSessionHolder.getUser()`로 어디서든 조회 가능 (서비스 계층에서 주로 사용).
- **역할**: `UserType` 기준 `ROLE_ADMIN`, `ROLE_STUDENT`.
- **Spring Security 경로 규칙** (`SecurityConfig`):
  ```
  /auth/**              permitAll
  /admin/**             hasRole("ADMIN")
  /student/**           hasRole("STUDENT")
  /user, /chatbot, /grading, /problems, /course, /contest, /notice  authenticated
  /swagger-ui/**, /v3/api-docs/**  permitAll
  나머지                permitAll
  ```
- 세션은 STATELESS, CSRF / formLogin / logout 전부 disable.
- 인증 실패 → `HttpStatusEntryPoint(401)`, 권한 부족 → `JwtAccessDeniedHandler(403)`.

### 토큰 정책 (`application.yml`)
- Access / Refresh 만료 시간은 `application.yml`의 `jwt.accessExp` / `jwt.refreshExp` 값을 참조.
- `jwt.secretKey`는 환경변수 `SECRET_KEY`에서 주입.

## 6. API 라우팅 요약

루트 prefix 기준(실제 파라미터/본문은 Swagger 참조).

| Prefix | Controller | 비고 |
|---|---|---|
| `/auth` | `AuthController` | 회원가입, 로그인, 리프레시 |
| `/user`, `/user/activity` | `UserController`, `UserActivityController` | 본인 정보, 활동, 목록 |
| `/admin/user` | `UserAdminController` | 관리자 회원 삭제 |
| `/problems` | `ProblemController` | 문제 조회/검색/필터 |
| `/admin/problems` | `ProblemAdminController` | 문제 CRUD |
| `/solve` | `JudgeController` | 채점 / 테스트 / 저장코드 |
| `/course` | `CourseController` | 코스 조회/검색 |
| `/student/course` | `CourseStudentController` | 참여 / 진행 / 완료 / 참여가능 |
| `/admin/course` | `CourseAdminController` | 코스 CRUD, 문제 매핑 |
| `/contest` | `ContestController`, `ContestSseController` | 조회 + SSE 구독 |
| `/student/contest` | `ContestStudentController` | 참가 |
| `/admin/contest` | `ContestAdminController` | 대회/문제/배점/참여자 관리 |
| `/notice` | `NoticeController` | 공지 조회 |
| `/admin/notice` | `NoticeAdminController` | 공지 CRUD, 파일 업로드 |
| `/chatbot/chat` | `ChatbotController` | Gemini 챗봇 |

Swagger UI: **`http://<host>:8080/swagger-ui/index.html`** — `auto-tag-classes: true`, tag/operation 알파벳 정렬.

## 7. 환경 변수 & 설정

`src/main/resources/application.yml`은 **Git에 커밋되지 않습니다** (`.gitignore`에 `application.yml`, `application-*.yml` 등록). 로컬/서버 실행 전 다음 환경변수를 주입하거나 로컬 `application.yml`을 복원해야 합니다.

| 키 | 설명 |
|---|---|
| `DB_URL` | JDBC URL (예: `jdbc:mariadb://host:3306/dukkaebi`) |
| `DB_USER` / `DB_PW` | DB 접속 정보 |
| `REDIS_HOST` / `REDIS_PORT` | Redis |
| `SECRET_KEY` | JWT 서명 키 (충분히 긴 랜덤 문자열) |
| `BUCKET_NAME` | S3 버킷 |
| `AWS_ACCESS_KEY` / `AWS_SECRET_KEY` | S3 IAM 자격 증명 |
| `GEMINI_API_KEY` | 챗봇용 Gemini 키 |
| `GEMINI_GRADING_API_KEY` | 채점 보조용 Gemini 키 (챗봇과 분리) |

## 8. 로컬 실행 방법

### 사전 준비
1. **JDK 21** 설치 (`java -version`)
2. **MariaDB** 실행 (로컬 or Docker). 스키마는 빈 DB면 됩니다(`ddl-auto: update`가 자동 생성).
3. **Redis** 실행 (기본 `localhost:6379`)
4. **Python3 / g++** — 로컬에서 채점(`/solve/test`, `/solve/grading`)을 테스트할 계획이면 필요합니다 (`CodeExecutor`가 시스템 `javac` / `python3` / `g++`을 직접 실행).
5. `src/main/resources/application.yml`을 생성하고 위의 환경변수를 채우기. (이 파일은 gitignore 대상이니 팀에서 공유되는 secret 문서 참고)

### 실행
```bash
./gradlew bootRun
# 또는
./gradlew clean build
java -jar build/libs/dukkaebi-0.0.1-SNAPSHOT.jar
```

기본 포트는 `8080` (Spring 기본값, 별도 설정 없음).

### 테스트
```bash
./gradlew test
```
(현재는 `DukkaebiApplicationTests.contextLoads()` 하나만 있어 실질적인 테스트 커버리지는 없습니다.)

## 9. 빌드 & 배포 파이프라인

**`.github/workflows/dukkaebi-github-actions.yml`** — `main` 브랜치 push/PR 트리거.

1. **체크아웃 + JDK 21 세팅.**
2. `microsoft/variable-substitution@v1` 로 `application.yml`의 플레이스홀더를 GitHub Secrets로 치환.
3. Gradle 빌드 — push는 `bootJar -x test`, PR은 `build` (테스트 포함).
4. `if: push` — Docker 이미지 빌드 후 DockerHub 푸시.
5. `appleboy/ssh-action`으로 배포 호스트에 접속해 기존 컨테이너를 교체 실행합니다. (상세 스크립트는 워크플로 파일 참조)

### 필요한 GitHub Secrets
`DB_URL`, `DB_USER`, `DB_PW`, `REDIS_HOST`, `REDIS_PORT`, `SECRET_KEY`,
`GEMINI_API_KEY`, `GEMINI_GRADING_API_KEY`,
`BUCKET_NAME`, `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`,
`DOCKER_USERNAME`, `DOCKER_PASSWORD`,
`EC2_HOST`, `EC2_USERNAME`, `EC2_KEY`, `EC2_PORT`

### Dockerfile 요점
- Base: `eclipse-temurin:21`
- `python3`, `python3-pip`, `g++`, `build-essential` 설치 → **컨테이너 내부에서 Python/C++ 코드 채점이 가능**.
- 엔트리포인트: `java -jar -Dspring.profiles.active=prod app.jar`

## 10. 채점 시스템 동작 원리

핵심 클래스:
- `domain/grading/util/CodeExecutor.java` — 언어별 실행기
- `domain/grading/service/JudgeService.java` — 채점 & 점수 집계

### 지원 언어
`java`, `python` / `python3`, `cpp` / `c++`. (그 외 언어 전달 시 `"지원하지 않는 언어입니다"` 리턴)

### 실행 절차 (Java 예시)
1. `System.getProperty("java.io.tmpdir")` 아래 임시 디렉토리 생성.
2. `Main.java` 에 사용자 코드 기록 (이스케이프 시퀀스 `\n`, `\t` 등을 실제 문자로 `normalizeCode` 변환).
3. `javac -encoding UTF-8 Main.java` 로 컴파일 — 10초 타임아웃, 실패 시 stderr를 `컴파일 에러:` 로 래핑 반환.
4. `java -cp <tempDir> Main` 실행. 테스트케이스의 `input`을 stdin으로 주입.
5. 실행 타임아웃(기본 **5초**) 초과 시 `destroyForcibly()` + `TIME_LIMIT_EXCEEDED`.
6. stdout / stderr를 읽고 `finally`에서 임시 디렉토리 삭제.

Python은 `python3` → `python` → `py` 순으로 자동 감지. C++는 `g++ -std=c++17`로 컴파일 후 실행.

### 채점 흐름 (`judgeCode`)
1. `Problem` + `List<ProblemTestCase>` 로드 (테스트케이스 없으면 예외).
2. 테스트케이스를 **순차 실행**. 첫 번째 오답 / 런타임 에러 / TLE 발생 시 **즉시 중단 (백준 스타일)**.
3. 출력 비교는 `normalizeOutput` — `trim`, CRLF→LF, 끝 공백 제거.
4. 상태 집계 → `JudgeStatus` (`ACCEPTED` / `WRONG_ANSWER` / `RUNTIME_ERROR` / `TIME_LIMIT_EXCEEDED`).
5. **정답 & 일반 문제** (`contestId == null`)인 경우:
   - 난이도별 점수(`difficultyToScore`) 부여 → `User.addScore()` → GrowthType 자동 갱신.
   - `UserActivityService.increaseTodaySolvedCount(1)` — 일일 활동 +1 (streak용).
6. `Problem.attemptCount += 1`, 정답이면 `solvedCount += 1` 업데이트.
7. `ProblemHistory` upsert — 사용자별 `SOLVED` / `FAILED` 기록.
8. **대회 문제**(`contestId != null`)이면:
   - `ContestParticipant`의 `totalScore`, `totalTime`을 **모든 문제 점수 재합산하여** 갱신 (부분 점수/시간 보정 가능).
   - `ContestSubmission`을 최신 코드로 업데이트(없으면 신규).
9. `JudgeResultRes` 반환 — 상태, `passedCount/total`, 총 실행시간, 에러 메시지, 케이스별 결과.

### `testCode` 와의 차이
`testCode`는 **부수효과 없음**. 점수/히스토리/통계/대회집계 전부 스킵하고 결과만 돌려줍니다.


## 11. 외부 연동

| 시스템 | 용도 | 관련 코드 |
|---|---|---|
| **MariaDB** | 영속 저장소. JPA + `ddl-auto: update`로 스키마 자동 관리. | `application.yml`, 각 도메인 `*JpaRepo` |
| **Redis** | Refresh 토큰, 챗봇 캐시/레이트리밋 상태 등 | `JwtTokenService`, `spring-data-redis` |
| **AWS S3** | 공지 첨부파일, 대회 이미지 업로드 | `global/common/service/S3Service.java`, `S3Config` |
| **Gemini 2.0 Flash** | 챗봇 응답 생성 (`GEMINI_API_KEY`) + 채점/해설 보조 (`GEMINI_GRADING_API_KEY`) | `GeminiService` |
| **DockerHub** | CI에서 이미지 푸시 | GitHub Actions |
| **EC2** | 운영 호스트. SSH 배포. | GitHub Actions 마지막 스텝 |

## 12. 운영 주의사항

1. **DB 스키마 관리** — JPA `ddl-auto: update`를 사용합니다. 엔티티 변경이 스키마에 영향을 주므로 엔티티 관련 PR은 리뷰를 엄격히 하고, 장기적으로 마이그레이션 툴(Flyway/Liquibase) 도입을 고려하세요.
2. **`application.yml` 비커밋 정책** — 로컬 설정은 secret 공유 절차(Notion/1Password 등)를 통해 전달받아야 합니다. git에 커밋하지 말 것.
3. **테스트 커버리지** — 현재 `DukkaebiApplicationTests.contextLoads()` 외에 실질 테스트가 없습니다. 신규 기능에는 최소 usecase 단위 테스트 동반을 권장합니다.
4. **SSE 타임아웃** — `spring.mvc.async.request-timeout: 1800000` (30분). 장시간 대회 구독 시 클라이언트에서 재연결 로직이 필요합니다.
5. **`dump.rdb` 커밋 금지** — 과거 커밋 이력에 Redis 덤프 파일이 올라간 적이 있어 제거했습니다(`2463d80`). 로컬 Redis 파일이 워킹 디렉토리에 생기지 않도록 주의하세요.
