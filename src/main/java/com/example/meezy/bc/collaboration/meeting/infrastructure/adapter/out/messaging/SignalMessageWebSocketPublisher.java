package com.example.meezy.bc.collaboration.meeting.infrastructure.adapter.out.messaging;

import com.example.meezy.bc.collaboration.meeting.application.port.out.SignalMessagePublisher;
import com.example.meezy.bc.collaboration.meeting.application.service.dto.request.SignalMessage;
import com.example.meezy.bc.collaboration.meeting.infrastructure.adapter.out.messaging.dto.SignalPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignalMessageWebSocketPublisher implements SignalMessagePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publish(UUID teamId, SignalMessage message) {
        UUID targetUserId = message.toUserId();

        if (targetUserId == null) {
            log.warn("시그널 메시지에 toUserId가 누락되었습니다. from={}", message.fromUserId());
            return;
        }

        String destination = "/topic/meeting/" + teamId + "/user/" + targetUserId;
        Object payload = createPayload(message);

        log.debug("시그널 전송: destination={}, type={}", destination, message.getClass().getSimpleName());

        messagingTemplate.convertAndSend(destination, payload);
    }

    private Object createPayload(SignalMessage message) {
        return switch (message) {
            case SignalMessage.Offer offer -> new SignalPayload(
                    "offer",
                    offer.fromUserId(),
                    offer.toUserId(),
                    offer.sdp(),
                    null,
                    null,
                    null
            );
            case SignalMessage.Answer answer -> new SignalPayload(
                    "answer",
                    answer.fromUserId(),
                    answer.toUserId(),
                    answer.sdp(),
                    null,
                    null,
                    null
            );
            case SignalMessage.IceCandidate ice -> new SignalPayload(
                    "ice-candidate",
                    ice.fromUserId(),
                    ice.toUserId(),
                    null,
                    ice.candidate(),
                    ice.sdpMid(),
                    ice.sdpMLineIndex()
            );
        };
    }
}
