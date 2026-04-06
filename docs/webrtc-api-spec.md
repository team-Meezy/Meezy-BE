# WebRTC API 명세서

이 문서는 현재 서버 구현을 기준으로 WebRTC 관련 API만 정리한 명세서다.
기준 코드:
- `src/main/java/com/example/meezy/bc/collaboration/meeting/**`
- `src/main/java/com/example/meezy/config/websocket/**`
- `src/main/resources/application.yml`

## 1. 개요

현재 WebRTC 기능은 아래 3개 층으로 나뉜다.

1. REST API
회의 생성, 활성 회의 조회, 회의 참여, 회의 퇴장, 녹음 업로드를 처리한다.

2. STOMP over WebSocket
WebRTC 시그널링 메시지(`offer`, `answer`, `ice-candidate`)를 중계한다.

3. WebSocket 구독 이벤트
회의 참가/퇴장/종료 이벤트를 브로드캐스트한다.

## 2. 인증

### 2.1 REST API 인증

- 모든 회의 API는 인증이 필요하다.
- HTTP 헤더에 JWT를 포함해야 한다.
- 헤더명과 접두사는 `spring.jwt.header`, `spring.jwt.prefix` 설정값을 따른다.
- 일반적으로 다음 형식을 기대한다.

```http
Authorization: Bearer {accessToken}
```

### 2.2 WebSocket/STOMP 인증

- WebSocket 연결 후 STOMP `CONNECT` 프레임의 native header 에 JWT를 포함해야 한다.
- 헤더명과 접두사는 REST와 동일하게 `spring.jwt.header`, `spring.jwt.prefix` 설정값을 따른다.

예시:

```text
CONNECT
Authorization: Bearer {accessToken}
```

- JWT 검증 실패 시 연결 또는 메시지 처리가 거부된다.

## 3. 공통 경로 규칙

### 3.1 REST Base Path

```text
/teams/{teamId}/meetings
```

### 3.2 STOMP Endpoint

- WebSocket endpoint 는 `chat.websocket.endpoint` 설정값을 사용한다.
- 보안 설정상 `/ws-chat/**` 경로가 허용되어 있어, 현재 배포에서는 `/ws-chat` 사용을 전제로 보는 것이 안전하다.
- SockJS 가 활성화되어 있다.

예시:

```text
ws(s)://{host}{chat.websocket.endpoint}/websocket
```

또는 SockJS client 사용 시:

```text
https://{host}{chat.websocket.endpoint}
```

### 3.3 STOMP Prefix

- 클라이언트 송신 prefix: `chat.websocket.app-prefix`
- 서버 브로커 prefix: `chat.websocket.broker-prefix`

현재 코드에서 사용하는 실제 destination 패턴은 아래와 같다.

- 클라이언트 송신: `/app/teams/{teamId}/meeting/signal`
- 시그널 수신: `/topic/meeting/{teamId}/user/{userId}`
- 회의 이벤트 수신: `/topic/meeting/{teamId}`

## 4. REST API

## 4.1 회의 시작

- Method: `POST`
- Path: `/teams/{teamId}/meetings`
- 설명: 팀장이 새 회의를 시작한다. 이미 활성 회의가 있으면 실패한다.
- 인증: 필요

### Path Parameter

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| `teamId` | UUID | 팀 ID |

### Request Body

없음

### Response

- Status: `200 OK`

```json
{
  "meetingId": "550e8400-e29b-41d4-a716-446655440000",
  "teamId": "11111111-1111-1111-1111-111111111111",
  "hostUserId": "22222222-2222-2222-2222-222222222222",
  "status": "ACTIVE",
  "startedAt": "2026-03-30T09:00:00",
  "participants": [
    {
      "participantId": "33333333-3333-3333-3333-333333333333",
      "userId": "22222222-2222-2222-2222-222222222222",
      "name": "host",
      "profileImageUrl": "https://cdn.example.com/profile.png",
      "joinedAt": "2026-03-30T09:00:00"
    }
  ],
  "iceServers": [
    {
      "urls": "stun:stun.l.google.com:19302",
      "username": null,
      "credential": null
    },
    {
      "urls": "turn:localhost:3478",
      "username": "turn-user",
      "credential": "turn-password"
    }
  ]
}
```

### 오류

| Status | ErrorCode | 조건 |
| --- | --- | --- |
| `404` | `TEAM_NOT_FOUND` | 팀이 없음 |
| `403` | `NOT_TEAM_LEADER` | 팀장이 아님 |
| `409` | `MEETING_ALREADY_EXISTS` | 이미 활성 회의가 존재 |

## 4.2 활성 회의 조회

- Method: `GET`
- Path: `/teams/{teamId}/meetings/active`
- 설명: 현재 팀의 활성 회의를 조회한다.
- 인증: 필요

### Response

- 활성 회의가 있으면 `200 OK`
- 활성 회의가 없으면 `204 No Content`

`200 OK` 응답 바디 구조는 `회의 시작`과 동일하다.

## 4.3 회의 참여

- Method: `POST`
- Path: `/teams/{teamId}/meetings/join`
- 설명: 현재 활성 회의에 참여한다.
- 인증: 필요

### Request Body

없음

### Response

- Status: `200 OK`
- 응답 바디 구조는 `회의 시작`과 동일하다.
- `participants` 는 현재 활성 참가자 목록이다.

### 동작 메모

- 이미 활성 참가자인 경우 서버는 조용히 현재 상태를 반환한다.
- 재입장인 경우 참가 이벤트가 다시 발행될 수 있다.

### 오류

| Status | ErrorCode | 조건 |
| --- | --- | --- |
| `404` | `MEETING_NOT_FOUND` | 활성 회의가 없음 |

## 4.4 회의 퇴장

- Method: `POST`
- Path: `/teams/{teamId}/meetings/leave`
- 설명: 현재 사용자를 활성 회의에서 퇴장 처리한다.
- 인증: 필요

### Request Body

없음

### Response

- Status: `200 OK`

```json
{
  "isMeetingActive": true
}
```

### 응답 필드

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| `isMeetingActive` | boolean | 퇴장 처리 후에도 회의가 계속 활성 상태인지 여부 |

### 동작 메모

- 마지막 참가자가 나가면 회의는 종료된다.
- 이 경우 별도로 `meeting-ended` 이벤트가 WebSocket 으로 발행된다.

### 오류

| Status | ErrorCode | 조건 |
| --- | --- | --- |
| `404` | `MEETING_NOT_FOUND` | 활성 회의가 없음 |

## 4.5 회의 녹음 업로드

- Method: `POST`
- Path: `/teams/{teamId}/meetings/{meetingId}/recording`
- 설명: 회의 녹음 파일을 업로드한다.
- 인증: 필요
- Content-Type: `multipart/form-data`
- 처리 방식: 동기 검증 후 비동기 처리, 응답은 즉시 `202 Accepted`

### Path Parameter

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| `teamId` | UUID | 팀 ID |
| `meetingId` | UUID | 회의 ID |

### Form Data

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `file` | file | Y | mp3 파일 |

### 제약

- 파일이 비어 있으면 안 된다.
- 파일명 확장자는 `.mp3` 여야 한다.
- `Content-Type` 은 `audio/mpeg` 여야 한다.

### Response

- Status: `202 Accepted`
- Body 없음

### 오류

| Status | ErrorCode | 조건 |
| --- | --- | --- |
| `400` | `INVALID_INPUT_VALUE` | `file` 누락 또는 validation 실패 |
| `400` | `EMPTY_AUDIO_FILE` | 빈 파일 |
| `400` | `INVALID_AUDIO_EXTENSION` | 확장자가 mp3 아님 |
| `400` | `INVALID_AUDIO_CONTENT_TYPE` | content-type 이 `audio/mpeg` 아님 |
| `404` | `MEETING_NOT_FOUND` | 후속 처리 중 회의 없음 |

주의:
- `ReceiveRecordingService` 는 `EmptyRecordingException`, `InvalidRecordingFormatException` 을 던지며, 실제 매핑되는 `ErrorCode` 명은 구현 클래스에 따라 달라질 수 있다.
- 클라이언트 명세 차원에서는 `빈 파일`, `형식 오류`, `회의 없음`으로 이해하면 된다.

## 5. STOMP 시그널링 API

## 5.1 클라이언트 송신 Destination

```text
/app/teams/{teamId}/meeting/signal
```

- 서버 매핑: `@MessageMapping("/teams/{teamId}/meeting/signal")`
- 설명: 특정 상대에게 WebRTC 시그널 메시지를 전달 요청한다.

## 5.2 메시지 타입

서버는 Jackson polymorphic type 으로 아래 3개 타입만 허용한다.

### 5.2.1 Offer

```json
{
  "type": "offer",
  "fromUserId": "22222222-2222-2222-2222-222222222222",
  "toUserId": "44444444-4444-4444-4444-444444444444",
  "sdp": "v=0\r\no=- 461173..."
}
```

### 5.2.2 Answer

```json
{
  "type": "answer",
  "fromUserId": "44444444-4444-4444-4444-444444444444",
  "toUserId": "22222222-2222-2222-2222-222222222222",
  "sdp": "v=0\r\no=- 461174..."
}
```

### 5.2.3 ICE Candidate

```json
{
  "type": "ice-candidate",
  "fromUserId": "22222222-2222-2222-2222-222222222222",
  "toUserId": "44444444-4444-4444-4444-444444444444",
  "candidate": "candidate:842163049 1 udp 1677729535 ...",
  "sdpMid": "0",
  "sdpMLineIndex": 0
}
```

### 필드 규칙

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `type` | string | Y | `offer`, `answer`, `ice-candidate` 중 하나 |
| `fromUserId` | UUID | Y | 보내는 사용자 ID. JWT 사용자와 반드시 같아야 함 |
| `toUserId` | UUID | Y | 받는 사용자 ID. 활성 회의 참가자여야 함 |
| `sdp` | string | Offer/Answer 시 필수 | SDP 본문 |
| `candidate` | string | ICE 시 필수 | candidate 문자열 |
| `sdpMid` | string | ICE 시 선택 | candidate 의 media id |
| `sdpMLineIndex` | number | ICE 시 선택 | candidate 의 m-line index |

## 5.3 서버 송신 Destination

```text
/topic/meeting/{teamId}/user/{userId}
```

- 서버는 `toUserId` 기준으로 대상 사용자 전용 토픽에 메시지를 발행한다.
- 브로드캐스트가 아니라 1:1 논리 채널이다.

### 서버 송신 Payload

서버가 내려주는 payload 구조는 아래와 같다.

```json
{
  "type": "offer",
  "fromUserId": "22222222-2222-2222-2222-222222222222",
  "toUserId": "44444444-4444-4444-4444-444444444444",
  "sdp": "v=0\r\no=- 461173...",
  "candidate": null,
  "sdpMid": null,
  "sdpMLineIndex": null
}
```

ICE candidate 예시:

```json
{
  "type": "ice-candidate",
  "fromUserId": "22222222-2222-2222-2222-222222222222",
  "toUserId": "44444444-4444-4444-4444-444444444444",
  "sdp": null,
  "candidate": "candidate:842163049 1 udp 1677729535 ...",
  "sdpMid": "0",
  "sdpMLineIndex": 0
}
```

## 5.4 시그널링 제약

서버는 시그널 relay 전에 아래 조건을 검증한다.

1. 초당 30개를 초과해 시그널을 보내면 안 된다.
2. `fromUserId` 는 현재 인증 사용자와 일치해야 한다.
3. 송신자는 해당 팀의 멤버여야 한다.
4. 해당 팀에는 활성 회의가 있어야 한다.
5. `toUserId` 는 활성 회의 참가자여야 한다.

## 5.5 시그널링 오류

WebSocket/STOMP 예외 응답 형식은 별도 표준화가 덜 되어 있으므로, 현재 서버 로직 기준 실패 조건만 명시한다.

| 의미 | ErrorCode |
| --- | --- |
| 활성 회의 없음 | `MEETING_NOT_FOUND` |
| 송신자와 JWT 사용자 불일치 | `SIGNAL_SENDER_MISMATCH` |
| 수신자가 회의 참가자가 아님 | `SIGNAL_RECIPIENT_NOT_IN_MEETING` |
| 팀 멤버가 아님 | `NOT_TEAM_MEMBER` |
| 시그널 속도 제한 초과 | `SIGNAL_RATE_LIMIT_EXCEEDED` |
| JWT 오류 | `INVALID_JWT` |

## 6. 회의 이벤트 구독 API

## 6.1 구독 Destination

```text
/topic/meeting/{teamId}
```

- 설명: 회의 참가/퇴장/종료 이벤트를 수신한다.

## 6.2 이벤트 타입

### 6.2.1 participant-joined

회의 참가 또는 재입장 시 발행된다.

```json
{
  "type": "participant-joined",
  "meetingId": "550e8400-e29b-41d4-a716-446655440000",
  "joinedUserId": "44444444-4444-4444-4444-444444444444",
  "existingParticipantIds": [
    "22222222-2222-2222-2222-222222222222"
  ],
  "joinedUserName": "alice",
  "joinedUserProfileImageUrl": "https://cdn.example.com/alice.png"
}
```

### 필드 설명

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `existingParticipantIds` | UUID 배열 | 새 참가자 입장 직전 기준 활성 참가자 목록 |

이 필드는 새 참가자가 누구에게 `offer` 를 보내야 하는지 결정할 때 사용할 수 있다.

### 6.2.2 participant-left

```json
{
  "type": "participant-left",
  "meetingId": "550e8400-e29b-41d4-a716-446655440000",
  "leftUserId": "44444444-4444-4444-4444-444444444444"
}
```

### 6.2.3 meeting-ended

마지막 참가자 퇴장 시 발행된다.

```json
{
  "type": "meeting-ended",
  "meetingId": "550e8400-e29b-41d4-a716-446655440000"
}
```

## 7. 권장 클라이언트 시퀀스

1. `GET /teams/{teamId}/meetings/active` 로 활성 회의 존재 여부를 확인한다.
2. 활성 회의가 없고 팀장이라면 `POST /teams/{teamId}/meetings` 로 회의를 시작한다.
3. 일반 참가자는 `POST /teams/{teamId}/meetings/join` 으로 참여한다.
4. WebSocket 에 연결하고 STOMP `CONNECT` 에 JWT 헤더를 넣는다.
5. `/topic/meeting/{teamId}` 와 `/topic/meeting/{teamId}/user/{myUserId}` 를 구독한다.
6. `participant-joined` 이벤트의 `existingParticipantIds` 를 참고해 peer 연결을 만든다.
7. 시그널은 `/app/teams/{teamId}/meeting/signal` 로 전송한다.
8. 퇴장 시 `POST /teams/{teamId}/meetings/leave` 를 호출한다.

## 8. 비고

- ICE 서버 목록은 매 회의 관련 REST 응답마다 내려온다.
- ICE 서버 값은 `webrtc.ice-servers` 설정을 그대로 사용한다.
- WebSocket broker heartbeat 는 `10초/10초`, SockJS heartbeat 는 `25초`로 설정되어 있다.
- WebSocket message size limit 은 `128KB` 이다.
