package com.example.meezy.bc.collaboration.team.domain.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class CannotRemoveLeaderException extends CustomException {

    public CannotRemoveLeaderException() {
        super(ErrorCode.CANNOT_REMOVE_LEADER);
    }
}
