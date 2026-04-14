# Dukkaebi Server

> **English** · [한국어](./README.ko.md)

Backend server for **Dukkaebi**, a coding-learning platform. Students solve problems to earn points and level up through a series of "Dokkaebi" ranks, while instructors create courses, problems, and contests to manage their students. This document is a detailed guide for new joiners and hand-over.

---

## Table of Contents
1. [Service Overview](#1-service-overview)
2. [Tech Stack](#2-tech-stack)
3. [Project Structure](#3-project-structure)
4. [Domain Features](#4-domain-features)
5. [Authentication / Authorization](#5-authentication--authorization)
6. [API Routing Summary](#6-api-routing-summary)
7. [Environment Variables & Configuration](#7-environment-variables--configuration)
8. [Running Locally](#8-running-locally)
9. [Build & Deployment Pipeline](#9-build--deployment-pipeline)
10. [How the Grading System Works](#10-how-the-grading-system-works)
11. [External Integrations](#11-external-integrations)
12. [Operational Notes](#12-operational-notes)

---

## 1. Service Overview

- **Purpose**: A coding-learning web service. Students solve, test, and save problem submissions, ask questions to a chatbot (Dubi), join courses, and participate in contests. Admins (instructors) manage problems, courses, notices, contests, and members.
- **Core Concepts**
  - **Growth system**: A user's `score` automatically promotes them through `WISP → COPPER → IRON → SILVER → GOLD → JADE → GOD` (see `GrowthType.fromScore`).
  - **Problem difficulty**: `COPPER / IRON / SILVER / GOLD / JADE` — correct answers award 1 / 3 / 5 / 10 / 15 points respectively.
  - **Courses / Contests**: Instructors bundle problems and publish them. Contests are keyed by a 6-character join code and broadcast the live leaderboard over SSE.
- **Clients**: Separate student and admin web apps are deployed independently. Allowed origins are defined in `SecurityConfig`.

## 2. Tech Stack

| Category | Details |
|---|---|
| Language / Runtime | **Java 21** (Temurin) |
| Framework | **Spring Boot 3.5.7** (web, data-jpa, security, data-redis) |
| Build | Gradle (wrapper included) |
| Database | **MariaDB** (runtime `mariadb-java-client`; MySQL driver also bundled) |
| Cache / Session aid | **Redis** (`spring-boot-starter-data-redis`) |
| Auth | **JWT** (`jjwt 0.12.6`) — Access / Refresh tokens |
| Docs | **springdoc-openapi 2.7.0** (Swagger UI: `/swagger-ui/index.html`) |
| File storage | **AWS S3** (`spring-cloud-starter-aws 2.2.6.RELEASE`) |
| External AI | **Google Gemini 2.0 Flash** (chatbot + grading assistance) |
| Resilience | **Resilience4j** (RateLimiter + Retry for Gemini calls) |
| HTTP client | Apache HttpClient5 + RestTemplate |
| Container | Docker (base: `eclipse-temurin:21`, includes python3 + g++) |
| CI/CD | GitHub Actions → DockerHub → remote host via SSH |
| Realtime | Spring MVC SSE (live contest scoreboard; async timeout: 30 min) |

## 3. Project Structure

A domain-driven package layout. Each domain is split into `presentation / usecase / service / domain / error` layers.

```
src/main/java/com/ducami/dukkaebi
├── DukkaebiApplication.java           # Entry point
├── domain/
│   ├── auth/        # Sign-up, sign-in, token refresh
│   ├── user/        # User info, activity (streak/contributions), admin member management
│   ├── problem/     # Problem CRUD, test cases, submission history
│   ├── grading/     # Code grading (JudgeService) / testing / SavedCode
│   ├── course/      # Course CRUD, enrollment, progress
│   ├── contest/     # Contest CRUD, participation, submissions, realtime leaderboard (SSE)
│   ├── chatbot/     # Dubi chatbot (Gemini wrapper + cache)
│   └── notice/      # Announcements
└── global/
    ├── common/      # Common responses (Response/ResponseData/PageResponse), S3Service
    ├── config/      # SpringConfig, SwaggerConfig, S3Config, RestTemplateConfig
    ├── exception/   # CustomException, ErrorCode, GlobalExceptionHandler
    └── security/
        ├── SecurityConfig.java
        ├── auth/    # AuthDetails, UserSessionHolder (currently authenticated user)
        └── jwt/     # JwtFilter, JwtProvider, JwtExtractor, JwtTokenService, etc.
```

### Layer conventions
- **presentation/controller**: `@RestController`. Accepts requests and delegates to a `UseCase`.
- **usecase**: Transaction boundary. Orchestrates services / repositories to fulfill a single API use case.
- **service**: Single-responsibility business logic (e.g. `JudgeService`, `UserActivityService`, `GeminiService`).
- **domain**: JPA entities, `enums/`, `repo/` (JpaRepository interfaces).
- **error**: Per-domain `ErrorCode` implementations of the shared `ErrorCode` interface.

> **When adding a new API**: build the skeleton in the order `Controller → UseCase → Service → Repo`, and define request/response types as `record`s inside `presentation/dto/request` and `presentation/dto/response`. That is the existing convention.

## 4. Domain Features

### auth
Sign-up (`/auth/sign-up`), sign-in (`/auth/sign-in`), and refresh (`/auth/refresh`). A successful sign-in issues both Access and Refresh tokens. Refresh tokens are stored in Redis so they can be reissued on refresh or blacklisted on logout.

### user
- `/user` returns the caller's info. `/user/list`, `/user/info/{userId}`, and `/user/list/filter` expose listing / sorting / filtering.
- `/user/activity/contributions`, `/user/activity/streak` — GitHub-style contribution heatmap and daily streak.
- `/user/logout`, `/user/delete`.
- `/admin/user/delete/{userId}` for admin member removal.
- `GrowthType` is recomputed automatically inside `User.addScore()` whenever the score changes.

### problem
- Regular-problem CRUD at `/admin/problems`; read-only access at `/problems`, `/problems/filter`, `/problems/{id}`.
- Entities: `Problem`, `ProblemTestCase`, `ProblemHistory` (per-user `SOLVED` / `FAILED` / `NOT_SOLVED`).
- If `Problem.contestId` is non-null, the problem is treated as a "contest problem" — it does not award normal points and is only tallied toward contest scores.

### grading (`/solve`)
The code execution engine. See [How the Grading System Works](#10-how-the-grading-system-works) below.
- `POST /solve/grading` — real submission (affects score / history / statistics)
- `POST /solve/test` — dry run (no side effects)
- `POST /solve/save`, `GET /solve/saved/{problemId}` — save and restore work-in-progress code.

### course
- Admin (`/admin/course`): create / update / delete courses, add and remove course-problem mappings.
- Shared (`/course`): list, detail, search.
- Student (`/student/course`): join a course, list in-progress / completed / joinable courses.
- `CourseProgressService` computes per-user progress.

### contest
Join-code-based contests. The 6-character random code is generated by `CodeGenerator`.
- `/admin/contest`: create / update / end contests; add problems (import existing or create contest-only ones); adjust per-problem score, order, deletion; list participants; manual score correction; fetch individual submissions.
- `/contest`: list, detail, search.
- `/student/contest/{code}/join`: participation.
- `/contest/{code}/subscribe` (SSE): realtime scoreboard. Whenever a submission is tallied, `ContestSseService` broadcasts a `ContestUpdateEvent`. The SSE timeout is controlled by `spring.mvc.async.request-timeout: 1800000` (30 min) in `application.yml`.
- On grading, if the problem has a `contestId`, `JudgeService` recomputes `ContestParticipant.totalScore / totalTime` and upserts `ContestSubmission` (latest submitted code).

### chatbot (`/chatbot/chat`)
- `GeminiService` wraps the Gemini API (`gemini-2.0-flash:generateContent`).
- Resilience4j: 3 requests-per-second RateLimiter + up to 3 Retries with exponential backoff (triggered on `HttpClientErrorException.TooManyRequests`).
- A **5-second TTL in-memory cache** deduplicates identical questions.
- System prompt: the chatbot's name is "Dubi", it always speaks in polite Korean, and it refuses non-coding questions.

### notice
- `/notice`, `/notice/home`, `/notice/{id}`, `/notice/search` — read-only.
- `/admin/notice/create|update|delete`, `/admin/notice/upload-file` — admin only. Multipart uploads go to S3.

## 5. Authentication / Authorization

- `JwtFilter` reads the Access token from `Authorization: Bearer <token>` and populates the `SecurityContext`.
- The currently authenticated user can be fetched from anywhere via `UserSessionHolder.getUser()` (used mainly in the service layer).
- **Roles**: `ROLE_ADMIN` and `ROLE_STUDENT`, both derived from `UserType`.
- **Spring Security path rules** (`SecurityConfig`):
  ```
  /auth/**              permitAll
  /admin/**             hasRole("ADMIN")
  /student/**           hasRole("STUDENT")
  /user, /chatbot, /grading, /problems, /course, /contest, /notice  authenticated
  /swagger-ui/**, /v3/api-docs/**  permitAll
  others                permitAll
  ```
- Sessions are STATELESS; CSRF / formLogin / logout are all disabled.
- On authentication failure → `HttpStatusEntryPoint(401)`; on authorization failure → `JwtAccessDeniedHandler(403)`.

### Token policy (`application.yml`)
- Access and Refresh expirations live in `jwt.accessExp` / `jwt.refreshExp`.
- `jwt.secretKey` is injected from the `SECRET_KEY` environment variable.

## 6. API Routing Summary

Grouped by root prefix (see Swagger for request/response details).

| Prefix | Controller | Notes |
|---|---|---|
| `/auth` | `AuthController` | Sign-up, sign-in, refresh |
| `/user`, `/user/activity` | `UserController`, `UserActivityController` | Self info, activity, listing |
| `/admin/user` | `UserAdminController` | Admin member deletion |
| `/problems` | `ProblemController` | Problem list / search / filter |
| `/admin/problems` | `ProblemAdminController` | Problem CRUD |
| `/solve` | `JudgeController` | Grading / testing / saved code |
| `/course` | `CourseController` | Course list / search |
| `/student/course` | `CourseStudentController` | Join / in-progress / completed / joinable |
| `/admin/course` | `CourseAdminController` | Course CRUD, problem mapping |
| `/contest` | `ContestController`, `ContestSseController` | Read + SSE subscription |
| `/student/contest` | `ContestStudentController` | Join |
| `/admin/contest` | `ContestAdminController` | Contest / problem / scoring / participant management |
| `/notice` | `NoticeController` | Notice read |
| `/admin/notice` | `NoticeAdminController` | Notice CRUD, file upload |
| `/chatbot/chat` | `ChatbotController` | Gemini chatbot |

Swagger UI: **`http://<host>:8080/swagger-ui/index.html`** — with `auto-tag-classes: true` and alphabetical sorting of tags/operations.

## 7. Environment Variables & Configuration

`src/main/resources/application.yml` is **not committed to git** (`application.yml`, `application-*.yml` are in `.gitignore`). Before running locally or in production, you need to inject these environment variables or restore a local `application.yml`.

| Key | Description |
|---|---|
| `DB_URL` | JDBC URL (e.g. `jdbc:mariadb://host:3306/dukkaebi`) |
| `DB_USER` / `DB_PW` | Database credentials |
| `REDIS_HOST` / `REDIS_PORT` | Redis |
| `SECRET_KEY` | JWT signing key (a sufficiently long random string) |
| `BUCKET_NAME` | S3 bucket |
| `AWS_ACCESS_KEY` / `AWS_SECRET_KEY` | S3 IAM credentials |
| `GEMINI_API_KEY` | Chatbot Gemini key |
| `GEMINI_GRADING_API_KEY` | Grading-assistance Gemini key (separate from the chatbot key) |

Fixed values:
- DB driver: `org.mariadb.jdbc.Driver`
- JPA: `ddl-auto: update`, `show-sql: true`
- Gemini URL: `https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent`
- S3 region: `ap-northeast-2`

## 8. Running Locally

### Prerequisites
1. **JDK 21** installed (`java -version`).
2. **MariaDB** running locally or in Docker. An empty database works — `ddl-auto: update` will create the schema.
3. **Redis** running (defaults to `localhost:6379`).
4. **Python3 / g++** — needed only if you plan to exercise `/solve/test` or `/solve/grading` locally, since `CodeExecutor` invokes the system's `javac` / `python3` / `g++` directly.
5. Create `src/main/resources/application.yml` and fill in the environment values above. (This file is gitignored, so request the shared secrets from the team.)

### Run
```bash
./gradlew bootRun
# or
./gradlew clean build
java -jar build/libs/dukkaebi-0.0.1-SNAPSHOT.jar
```

The default port is `8080` (Spring default; not explicitly configured).

### Test
```bash
./gradlew test
```
(Only `DukkaebiApplicationTests.contextLoads()` exists today, so actual coverage is effectively zero.)

## 9. Build & Deployment Pipeline

**`.github/workflows/dukkaebi-github-actions.yml`** — triggered by `push` / `pull_request` to `main`.

1. Checkout and set up **JDK 21**.
2. Substitute `application.yml` placeholders with GitHub Secrets via `microsoft/variable-substitution@v1`.
3. Gradle build — `push` uses `bootJar -x test`, PRs run `build` (with tests).
4. `if: push` — build the Docker image and push to DockerHub.
5. `appleboy/ssh-action` connects to the deployment host and replaces the running container. (See the workflow file for the exact script.)

### Required GitHub Secrets
`DB_URL`, `DB_USER`, `DB_PW`, `REDIS_HOST`, `REDIS_PORT`, `SECRET_KEY`,
`GEMINI_API_KEY`, `GEMINI_GRADING_API_KEY`,
`BUCKET_NAME`, `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`,
`DOCKER_USERNAME`, `DOCKER_PASSWORD`,
`EC2_HOST`, `EC2_USERNAME`, `EC2_KEY`, `EC2_PORT`

### Dockerfile highlights
- Base: `eclipse-temurin:21`.
- Installs `python3`, `python3-pip`, `g++`, `build-essential` — so **Python / C++ grading runs inside the container**.
- Entry point: `java -jar -Dspring.profiles.active=prod app.jar`.

## 10. How the Grading System Works

Key classes:
- `domain/grading/util/CodeExecutor.java` — per-language runner
- `domain/grading/service/JudgeService.java` — grading & score aggregation

### Supported languages
`java`, `python` / `python3`, `cpp` / `c++`. Anything else returns `"unsupported language"`.

### Execution flow (Java example)
1. Create a temp directory under `System.getProperty("java.io.tmpdir")`.
2. Write the user's source into `Main.java`, normalizing escape sequences (`\n`, `\t`, …) into real characters via `normalizeCode`.
3. Compile with `javac -encoding UTF-8 Main.java` — 10s timeout; on failure, stderr is wrapped as `"컴파일 에러: …"` and returned.
4. Run `java -cp <tempDir> Main`, piping the test case's `input` into stdin.
5. If the execution times out (default **5s**), the process is `destroyForcibly()`-ed and the result becomes `TIME_LIMIT_EXCEEDED`.
6. Read stdout / stderr and clean up the temp directory in `finally`.

Python auto-detects `python3` → `python` → `py`. C++ is compiled via `g++ -std=c++17` and then executed.

### Grading flow (`judgeCode`)
1. Load the `Problem` and its `List<ProblemTestCase>` (throws if no test cases exist).
2. Run test cases **sequentially**. Stops immediately on the first wrong answer / runtime error / TLE (Baekjoon style).
3. Output comparison uses `normalizeOutput`: `trim`, CRLF → LF, trailing whitespace removed.
4. Status is aggregated into a `JudgeStatus` (`ACCEPTED` / `WRONG_ANSWER` / `RUNTIME_ERROR` / `TIME_LIMIT_EXCEEDED`).
5. **If accepted AND it is a regular problem** (`contestId == null`):
   - Award difficulty-based points (`difficultyToScore`) via `User.addScore()`, which also recomputes `GrowthType`.
   - Call `UserActivityService.increaseTodaySolvedCount(1)` to bump the daily activity counter (used by the streak feature).
6. Update `Problem.attemptCount += 1`, and `solvedCount += 1` if accepted.
7. Upsert `ProblemHistory` for the user with `SOLVED` / `FAILED`.
8. **If it is a contest problem** (`contestId != null`):
   - Recompute `ContestParticipant.totalScore` and `totalTime` by re-summing all of the participant's problem scores (so manual score/time corrections are supported).
   - Upsert `ContestSubmission` with the latest code.
9. Return `JudgeResultRes` — status, `passedCount/total`, total execution time, error message, and per-case results.

### Difference from `testCode`
`testCode` has **no side effects**. It skips score, history, statistics, and contest aggregation, and only returns the result.

> The code-execution isolation / resource-limit policy is documented in a separate internal handover note. Any hardening must precede opening this service up to untrusted users.

## 11. External Integrations

| System | Purpose | Relevant code |
|---|---|---|
| **MariaDB** | Primary store. JPA + `ddl-auto: update` manages the schema. | `application.yml`, each domain's `*JpaRepo` |
| **Redis** | Refresh tokens, chatbot cache / rate-limit state, etc. | `JwtTokenService`, `spring-data-redis` |
| **AWS S3** | Notice attachments, contest image uploads. | `global/common/service/S3Service.java`, `S3Config` |
| **Gemini 2.0 Flash** | Chatbot responses (`GEMINI_API_KEY`) + grading/explanation assistance (`GEMINI_GRADING_API_KEY`). | `GeminiService` |
| **DockerHub** | CI pushes container images here. | GitHub Actions |
| **Deploy host** | Runtime environment. Deployed via SSH. | GitHub Actions final step |

## 12. Operational Notes

> Sensitive production details, infra topology, and incident-response checklists live in a **private hand-over document**. This section only contains general notes a developer reading this repo should know.

1. **Schema management** — JPA `ddl-auto: update` is in use. Entity changes affect the live schema, so entity-related PRs need strict review. Long-term, adopting a migration tool (Flyway/Liquibase) is recommended.
2. **Do not commit `application.yml`** — local settings are exchanged out-of-band (Notion / 1Password / etc.).
3. **Test coverage** — nothing meaningful beyond `DukkaebiApplicationTests.contextLoads()`. New features should ship with at least use-case-level tests.
4. **SSE timeout** — `spring.mvc.async.request-timeout: 1800000` (30 min). Long-running contest subscriptions need reconnect logic on the client.
5. **Do not commit `dump.rdb`** — a Redis dump file was accidentally committed in the past and later removed (`2463d80`). Keep local Redis dumps out of the working tree.

---

## Hand-over Checklist

- [ ] JDK 21 installed and `./gradlew bootRun` works locally
- [ ] Access to MariaDB / Redis (local or shared dev) with credentials in hand
- [ ] `application.yml` secrets received and placed
- [ ] Main APIs verified via Swagger UI (`/swagger-ui/index.html`)
- [ ] Access to GitHub Secrets (CI/CD) for rollback / edits
- [ ] DockerHub and deployment host SSH access
- [ ] Access to the AWS S3 bucket / IAM policy
- [ ] Access to the Gemini API console (so keys can be rotated)
- [ ] Confirmed the production MariaDB backup policy (not yet documented — verify separately if needed)

If anything here drifts, update it right away. The code is the source of truth; this README is only the map.
