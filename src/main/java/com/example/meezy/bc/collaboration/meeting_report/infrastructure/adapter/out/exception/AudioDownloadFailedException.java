package com.example.meezy.bc.collaboration.meeting_report.infrastructure.adapter.out.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class AudioDownloadFailedException extends CustomException {

    public AudioDownloadFailedException() {
        super(ErrorCode.AUDIO_DOWNLOAD_FAILED);
    }
}
