package com.example.meezy.bc.collaboration.meeting.application.port.out;

import com.example.meezy.bc.collaboration.meeting.domain.event.MeetingEvent;

public interface MeetingEventPublisher {

    void publish(MeetingEvent event);
}
