package com.example.meezy.bc.team.team.infrastructure.adapter.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class FailedDeleteException extends CustomException {

    public FailedDeleteException() {
        super(ErrorCode.FAILED_DELETE);
    }
}
