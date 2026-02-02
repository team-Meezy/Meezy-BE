package com.example.meezy.bc.collaboration.participation_metrics.infrastructure.adapter.in;

import com.example.meezy.bc.collaboration.meeting.domain.event.MeetingEndedEvent;
import com.example.meezy.bc.collaboration.participation_metrics.application.service.internal.ParticipationMetricsGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEndedEventListener {

    private final ParticipationMetricsGenerator participationMetricsGenerator;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingEnded(MeetingEndedEvent event) {
        try {
            participationMetricsGenerator.generate(event.meetingId());
            log.info("참여 지표 생성 완료: meetingId={}", event.meetingId());
        } catch (Exception e) {
            log.error("참여 지표 생성 실패: meetingId={}", event.meetingId(), e);
        }
    }
}
