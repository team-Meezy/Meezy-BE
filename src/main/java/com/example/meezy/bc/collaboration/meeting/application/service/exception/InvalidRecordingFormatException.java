package com.example.meezy.bc.collaboration.meeting.application.service.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class InvalidRecordingFormatException extends CustomException {

    public InvalidRecordingFormatException() {
        super(ErrorCode.INVALID_AUDIO_EXTENSION);
    }
}
