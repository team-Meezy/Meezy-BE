package com.example.meezy.bc.collaboration.team.domain.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class InvalidInviteCodeException extends CustomException {

    public InvalidInviteCodeException() {
        super(ErrorCode.INVALID_INVITE_CODE);
    }
}
