package com.example.meezy.bc.collaboration.meeting.domain.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class MeetingAlreadyExistsException extends CustomException {

    public MeetingAlreadyExistsException() {
        super(ErrorCode.MEETING_ALREADY_EXISTS);
    }
}
