package com.example.meezy.bc.collaboration.participation_metrics.application.service.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class ParticipationSenderNotInMeetingException extends CustomException {

    public ParticipationSenderNotInMeetingException() {
        super(ErrorCode.PARTICIPATION_SENDER_NOT_IN_MEETING);
    }
}
