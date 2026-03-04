package com.example.meezy.bc.collaboration.meeting_report.infrastructure.adapter.out.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class AudioUploadFailedException extends CustomException {

    public AudioUploadFailedException() {
        super(ErrorCode.AUDIO_UPLOAD_FAILED);
    }
}
