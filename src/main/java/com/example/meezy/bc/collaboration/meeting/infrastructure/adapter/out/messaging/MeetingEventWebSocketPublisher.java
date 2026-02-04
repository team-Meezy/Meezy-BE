package com.example.meezy.bc.collaboration.meeting.infrastructure.adapter.out.messaging;

import com.example.meezy.bc.collaboration.meeting.application.port.out.MeetingEventPublisher;
import com.example.meezy.bc.collaboration.meeting.domain.event.MeetingEndedEvent;
import com.example.meezy.bc.collaboration.meeting.domain.event.MeetingEvent;
import com.example.meezy.bc.collaboration.meeting.domain.event.ParticipantJoinedEvent;
import com.example.meezy.bc.collaboration.meeting.domain.event.ParticipantLeftEvent;
import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEventWebSocketPublisher implements MeetingEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @Override
    public void publish(MeetingEvent event) {
        UUID teamId = event.teamId();
        String destination = "/topic/meeting/" + teamId;

        Object payload = createPayload(event);

        log.debug("미팅 이벤트 브로드캐스트: destination={}, event={}", destination, event.getClass().getSimpleName());

        messagingTemplate.convertAndSend(destination, payload);
    }

    private Object createPayload(MeetingEvent event) {
        return switch (event) {
            case ParticipantJoinedEvent e -> createParticipantJoinedPayload(e);
            case ParticipantLeftEvent e -> Map.of(
                    "type", "participant-left",
                    "meetingId", e.meetingId(),
                    "leftUserId", e.leftUserId()
            );
            case MeetingEndedEvent e -> Map.of(
                    "type", "meeting-ended",
                    "meetingId", e.meetingId()
            );
        };
    }

    private Map<String, Object> createParticipantJoinedPayload(ParticipantJoinedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "participant-joined");
        payload.put("meetingId", event.meetingId());
        payload.put("joinedUserId", event.joinedUserId());
        payload.put("existingParticipantIds", event.existingParticipantIds());

        userRepository.findByUserId_Value(event.joinedUserId())
                .ifPresent(user -> {
                    payload.put("joinedUserName", user.getName());
                    payload.put("joinedUserProfileImageUrl", user.getProfileImageUrl());
                });

        return payload;
    }
}
