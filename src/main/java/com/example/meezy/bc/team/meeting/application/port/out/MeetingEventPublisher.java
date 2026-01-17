package com.example.meezy.bc.team.meeting.application.port.out;

import com.example.meezy.bc.team.meeting.domain.event.MeetingEvent;

public interface MeetingEventPublisher {

    void publish(MeetingEvent event);
}
