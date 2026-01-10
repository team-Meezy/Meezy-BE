package com.example.meezy.bc.user.application.service.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class InvalidVerificationCodeException extends CustomException {

    public InvalidVerificationCodeException() {
        super(ErrorCode.INVALID_VERIFICATION_CODE);
    }
}
