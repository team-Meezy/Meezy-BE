# Code Style Guide

이 문서는 Meezy 프로젝트의 상세 코드 스타일 가이드입니다.

## Exception Handling

모든 커스텀 예외는 `CustomException`을 상속하고, `ErrorCode` enum을 통해 HTTP 상태와 메시지를 관리한다.

```java
// sharedkernel/exception/CustomException.java
public abstract class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

// 구체적인 예외 정의 (도메인별 exception 패키지에 위치)
public class MeetingNotFoundException extends CustomException {
    public MeetingNotFoundException() {
        super(ErrorCode.MEETING_NOT_FOUND);
    }
}

// sharedkernel/exception/ErrorCode.java
public enum ErrorCode {
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회의를 찾을 수 없습니다."),
    MEETING_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "회의가 진행 중이 아닙니다."),
    // ...
}
```

`GlobalExceptionHandler`가 `@ControllerAdvice`로 모든 `CustomException`을 `ErrorResponse`로 변환한다.

## Port/Adapter Pattern (Output Only)

이 프로젝트는 **Output Port만 사용**한다. Input Port(Use Case 인터페이스)는 사용하지 않고, Application Service를 직접 호출한다.

```
application/
├── service/           # Use Case 구현 (인터페이스 없음)
└── port/
    └── out/           # Output Port 인터페이스만 정의

infrastructure/
└── adapter/
    └── out/           # Output Port 구현체
```

**예시:**
```java
// application/port/out/FileStoragePort.java
public interface FileStoragePort {
    String upload(MultipartFile file);
    void deleteByKey(String key);
    String update(String oldKey, MultipartFile newFile);
}

// infrastructure/adapter/out/S3FileStorageAdapter.java
@Service
@RequiredArgsConstructor
public class S3FileStorageAdapter implements FileStoragePort {
    private final S3Client s3Client;
    // 구현...
}
```

## Domain Modeling with JPA

도메인 엔티티는 JPA 어노테이션을 직접 사용하며, Aggregate Root는 `AbstractAggregateRoot`를 상속한다.

### Entity 기본 구조

```java
@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_meeting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Meeting extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "meeting_id"))
    private MeetingId meetingId;

    // 정적 팩토리 메서드로 생성
    public static Meeting start(TeamId teamId, UserId hostUserId) {
        Meeting meeting = Meeting.builder()
                .meetingId(MeetingId.newId())
                // ...
                .build();
        meeting.registerEvent(new MeetingStartedEvent(...));
        return meeting;
    }

    // 비즈니스 로직은 도메인 메서드로
    public void join(UserId userId) {
        validateActive();
        // ...
        registerEvent(new ParticipantJoinedEvent(...));
    }
}
```

### Value Object (record + @Embeddable)

```java
@Embeddable
public record MeetingId(UUID value) {
    public static MeetingId newId() {
        return new MeetingId(UUID.randomUUID());
    }
    public static MeetingId of(UUID meetingId) {
        return new MeetingId(meetingId);
    }
}

// 복합 Value Object
@Embeddable
public record InviteCode(String code, LocalDateTime expiresAt) {
    public static InviteCode generate() {
        return new InviteCode(
            UUID.randomUUID().toString().substring(0, 8),
            LocalDateTime.now().plusMinutes(5)
        );
    }
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
```

### AbstractAggregateRoot

```java
public abstract class AbstractAggregateRoot {
    private final List<Object> domainEvents = new ArrayList<>();

    protected void registerEvent(Object event) {
        this.domainEvents.add(event);
    }

    public List<Object> pullDomainEvents() {
        List<Object> events = List.copyOf(this.domainEvents);
        domainEvents.clear();
        return events;
    }
}
```

### Domain Event

```java
public record MeetingEndedEvent(UUID teamId, UUID meetingId) implements MeetingEvent {}
```

