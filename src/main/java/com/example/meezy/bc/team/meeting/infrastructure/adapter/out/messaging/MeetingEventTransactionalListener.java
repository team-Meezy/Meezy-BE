package com.example.meezy.bc.team.meeting.infrastructure.adapter.out.messaging;

import com.example.meezy.bc.team.meeting.application.port.out.MeetingEventPublisher;
import com.example.meezy.bc.team.meeting.domain.event.MeetingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MeetingEventTransactionalListener {

    private final MeetingEventPublisher meetingEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingEvent(MeetingEvent event) {
        meetingEventPublisher.publish(event);
    }
}
