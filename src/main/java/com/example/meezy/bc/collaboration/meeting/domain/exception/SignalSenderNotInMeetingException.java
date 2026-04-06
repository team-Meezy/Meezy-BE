package com.example.meezy.bc.collaboration.meeting.domain.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class SignalSenderNotInMeetingException extends CustomException {

    public SignalSenderNotInMeetingException() {
        super(ErrorCode.SIGNAL_SENDER_NOT_IN_MEETING);
    }
}
