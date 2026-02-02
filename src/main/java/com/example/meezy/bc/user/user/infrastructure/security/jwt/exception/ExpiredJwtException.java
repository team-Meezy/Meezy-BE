package com.example.meezy.bc.user.user.infrastructure.security.jwt.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class ExpiredJwtException extends CustomException {

    public ExpiredJwtException(){
        super(ErrorCode.EXPIRED_JWT);
    }
}
