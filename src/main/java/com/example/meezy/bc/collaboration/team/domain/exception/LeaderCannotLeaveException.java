package com.example.meezy.bc.collaboration.team.domain.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class LeaderCannotLeaveException extends CustomException {

    public LeaderCannotLeaveException() {
        super(ErrorCode.LEADER_CANNOT_LEAVE);
    }
}
