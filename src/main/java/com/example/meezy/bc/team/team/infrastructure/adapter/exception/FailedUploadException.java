package com.example.meezy.bc.team.team.infrastructure.adapter.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class FailedUploadException extends CustomException {

    public FailedUploadException() {
        super(ErrorCode.FAILED_UPLOAD);
    }
}
