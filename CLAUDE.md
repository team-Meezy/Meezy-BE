# CLAUDE.md

**Meezy** : 회의 자체에만 집중할 수 있는 서비스
- 온라인 화상 회의 진행 (WebRTC Signaling)
- AI 기반 회의 내용 요약 및 피드백 생성
- 실시간 채팅 및 참여도 추적

## Build Commands

```bash
./gradlew build          # Build
./gradlew bootRun        # Run
./gradlew test           # Test all
./gradlew test --tests "com.example.meezy.architecture.*"      # Architecture tests
./gradlew test --tests "com.example.meezy.bc.collaboration.*"  # Specific BC tests
```

## Architecture

Spring Boot 3.4.1 (Java 21) + **Hexagonal Architecture** + **DDD**

```
com.example.meezy
├── bc/
│   ├── user/                         
│   │   ├── user/                     # 인증 (OAuth2, JWT, 이메일 인증)
│   ├── collaboration/
│   │   ├── team/                     # 팀 관리, 초대 코드
│   │   ├── meeting/                  # 회의 생명주기, WebRTC Signaling
│   │   ├── chat_room/, chat_message/ # 실시간 채팅 (RabbitMQ)
│   │   ├── meeting_report/           # AI 요약/피드백 (OpenAI)
│   │   └── participation_metrics/    # 참여도 추적 (WebSocket, Redis)
│   └── sharedkernel/                 # AbstractAggregateRoot, CustomException, ErrorCode
└── config/                           # security, websocket, messaging, ai, file, exception
```

**레이어:** `domain/` → `application/` → `infrastructure/` → `presentation/`

## Key Endpoints

| 기능 | 프로토콜 | 경로 |
|------|----------|------|
| 회의 Signaling | WebSocket | `/app/teams/{teamId}/meeting/signal` |
| 참여도 기록 | WebSocket | `/app/meetings/{meetingId}/participation/voice\|chat` |
| 채팅 | WebSocket + RabbitMQ | STOMP broker |
| REST API | HTTP | `/teams/**`, `/auth/**` |

## Key Integrations

- **Auth**: OAuth2 (Google, Kakao, Naver) + JWT
- **Real-time**: WebSocket (STOMP) + RabbitMQ
- **AI**: OpenAI (GPT 요약/피드백, Whisper 음성→텍스트)
- **Storage**: S3 (Scaleway Garage)
- **DB**: MySQL 8 / Redis

## Code Style

**Exception:** `CustomException` 상속 + `ErrorCode` enum → `GlobalExceptionHandler`

**Port/Adapter:** Output Port만 사용 (`application/port/out/` → `infrastructure/adapter/out/`)

**Entity:** `@Builder(access=PRIVATE)` + `@NoArgsConstructor(access=PROTECTED)` + static factory + `AbstractAggregateRoot` 상속

**Value Object:** `record` + `@Embeddable` + `newId()`, `of()` 정적 메서드

**Domain Event:** `record` 사용

상세 코드 예시: @docs/CODE_STYLE.md

## 주의 사항

- Input Port 만들지 말 것 (Output Port만 사용)
- 아키텍처 규칙은 `HexagonalArchitectureTest`, `CyclicDependencyTest`에서 강제됨

