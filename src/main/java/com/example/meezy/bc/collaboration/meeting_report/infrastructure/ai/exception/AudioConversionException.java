package com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class AudioConversionException extends CustomException {

    public AudioConversionException() {
        super(ErrorCode.AUDIO_CONVERSION_FAILED);
    }
}
