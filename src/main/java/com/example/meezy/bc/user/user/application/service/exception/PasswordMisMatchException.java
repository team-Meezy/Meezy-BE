package com.example.meezy.bc.user.user.application.service.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class PasswordMisMatchException extends CustomException {
    public PasswordMisMatchException(){
        super(ErrorCode.PASSWORD_MISMATCH);
    }
}
