package com.example.meezy.bc.team.meeting_report.infrastructure.ai.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class EmptyAudioFileException extends CustomException {

    public EmptyAudioFileException() {
        super(ErrorCode.EMPTY_AUDIO_FILE);
    }
}
