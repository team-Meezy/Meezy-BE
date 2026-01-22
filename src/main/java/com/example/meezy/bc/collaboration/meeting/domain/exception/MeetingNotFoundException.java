package com.example.meezy.bc.collaboration.meeting.domain.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class MeetingNotFoundException extends CustomException {

    public MeetingNotFoundException() {
        super(ErrorCode.MEETING_NOT_FOUND);
    }
}
