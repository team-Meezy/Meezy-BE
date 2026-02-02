package com.example.meezy.bc.collaboration.meeting.infrastructure.adapter.out.messaging;

import com.example.meezy.bc.collaboration.meeting.application.port.out.MeetingEventPublisher;
import com.example.meezy.bc.collaboration.meeting.domain.event.MeetingEndedEvent;
import com.example.meezy.bc.collaboration.meeting.domain.event.MeetingEvent;
import com.example.meezy.bc.collaboration.meeting.domain.event.ParticipantJoinedEvent;
import com.example.meezy.bc.collaboration.meeting.domain.event.ParticipantLeftEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEventWebSocketPublisher implements MeetingEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publish(MeetingEvent event) {
        UUID teamId = event.teamId();
        String destination = "/topic/meeting/" + teamId; //해당 경로를 구독하는 사용자에게 브로드케스팅할 주소

        Object payload = createPayload(event);

        log.debug("미팅 이벤트 브로드캐스트: destination={}, event={}", destination, event.getClass().getSimpleName());

        messagingTemplate.convertAndSend(destination, payload);
    }

    private Object createPayload(MeetingEvent event) {
        return switch (event) {
            //회의에 새롭게 참여했을 때
            case ParticipantJoinedEvent e -> Map.of(
                    "type", "participant-joined", //회의 참여 알림
                    "meetingId", e.meetingId(),
                    "joinedUserId", e.joinedUserId(),
                    "existingParticipantIds", e.existingParticipantIds() //이미 회의에 존재하는 UserId
            );
            //회의를 나갔을 때
            case ParticipantLeftEvent e -> Map.of(
                    "type", "participant-left", //회의 나가기 알림
                    "meetingId", e.meetingId(),
                    "leftUserId", e.leftUserId()
            );
            //회의가 종료 되었을 때
            case MeetingEndedEvent e -> Map.of(
                    "type", "meeting-ended", //호의 종료 알림
                    "meetingId", e.meetingId()
            );
        };
    }
}
