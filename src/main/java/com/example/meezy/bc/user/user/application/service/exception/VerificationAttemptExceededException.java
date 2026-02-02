package com.example.meezy.bc.user.user.application.service.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class VerificationAttemptExceededException extends CustomException {

    public VerificationAttemptExceededException() {
        super(ErrorCode.VERIFICATION_ATTEMPT_EXCEEDED);
    }
}
