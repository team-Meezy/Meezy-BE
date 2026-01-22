package com.example.meezy.bc.collaboration.meeting.infrastructure.adapter.out.messaging;

import com.example.meezy.bc.collaboration.meeting.application.port.out.MeetingEventPublisher;
import com.example.meezy.bc.collaboration.meeting.domain.event.MeetingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEventTransactionalListener {

    private final MeetingEventPublisher meetingEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingEvent(MeetingEvent event) {
        try {
            meetingEventPublisher.publish(event);
        } catch (Exception e) {
            log.error("미팅 이벤트 발행 실패: eventType={}, meetingId={}, teamId={}",
                    event.getClass().getSimpleName(),
                    event.meetingId(),
                    event.teamId(),
                    e);
        }
    }
}
