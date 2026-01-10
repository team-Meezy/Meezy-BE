package com.example.meezy.bc.user.application.service.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class DuplicateAccountIdException extends CustomException {

    public DuplicateAccountIdException(){
        super(ErrorCode.DUPLICATE_ACCOUNT_ID);
    }
}
