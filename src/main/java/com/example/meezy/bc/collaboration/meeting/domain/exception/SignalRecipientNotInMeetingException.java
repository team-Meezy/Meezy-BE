package com.example.meezy.bc.collaboration.meeting.domain.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class SignalRecipientNotInMeetingException extends CustomException {

    public SignalRecipientNotInMeetingException() {
        super(ErrorCode.SIGNAL_RECIPIENT_NOT_IN_MEETING);
    }
}
