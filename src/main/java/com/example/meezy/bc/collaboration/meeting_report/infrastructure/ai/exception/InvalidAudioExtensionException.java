package com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class InvalidAudioExtensionException extends CustomException {

    public InvalidAudioExtensionException() {
        super(ErrorCode.INVALID_AUDIO_EXTENSION);
    }
}
