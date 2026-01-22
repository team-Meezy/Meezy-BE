package com.example.meezy.bc.collaboration.meeting.domain.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class MeetingNotActiveException extends CustomException {

    public MeetingNotActiveException() {
        super(ErrorCode.MEETING_NOT_ACTIVE);
    }
}
