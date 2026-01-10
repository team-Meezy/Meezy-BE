package com.example.meezy.bc.user.application.service.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class DuplicateEmailException extends CustomException {

    public DuplicateEmailException(){
        super(ErrorCode.DUPLICATE_EMAIL);
    }
}
