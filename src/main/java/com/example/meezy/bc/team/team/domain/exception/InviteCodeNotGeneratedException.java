package com.example.meezy.bc.team.team.domain.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class InviteCodeNotGeneratedException extends CustomException {

    public InviteCodeNotGeneratedException() {
        super(ErrorCode.INVITE_CODE_NOT_GENERATED);
    }
}
