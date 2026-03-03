package com.example.meezy.bc.collaboration.meeting.application.service.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class EmptyRecordingException extends CustomException {

    public EmptyRecordingException() {
        super(ErrorCode.EMPTY_AUDIO_FILE);
    }
}
