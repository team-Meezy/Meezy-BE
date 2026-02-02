package com.example.meezy.bc.collaboration.team.domain.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class InviteCodeExpiredException extends CustomException {

    public InviteCodeExpiredException() {
        super(ErrorCode.INVITE_CODE_EXPIRED);
    }
}
