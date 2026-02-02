package com.example.meezy.bc.collaboration.team.infrastructure.adapter.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class InvalidExtensionException extends CustomException {

    public InvalidExtensionException() {
        super(ErrorCode.INVALID_EXTENSION);
    }
}
