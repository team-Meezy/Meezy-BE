package com.example.meezy.bc.team.meeting.domain.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class ParticipantNotFoundException extends CustomException {

    public ParticipantNotFoundException() {
        super(ErrorCode.PARTICIPANT_NOT_FOUND);
    }
}
