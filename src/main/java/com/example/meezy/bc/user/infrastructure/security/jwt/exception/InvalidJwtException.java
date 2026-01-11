package com.example.meezy.bc.user.infrastructure.security.jwt.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class InvalidJwtException extends CustomException {
    public InvalidJwtException() {
        super(ErrorCode.INVALID_JWT);
    }
}
