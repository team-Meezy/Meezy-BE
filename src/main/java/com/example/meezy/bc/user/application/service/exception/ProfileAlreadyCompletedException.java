package com.example.meezy.bc.user.application.service.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class ProfileAlreadyCompletedException extends CustomException {

    public ProfileAlreadyCompletedException() {
        super(ErrorCode.PROFILE_ALREADY_COMPLETED);
    }
}
