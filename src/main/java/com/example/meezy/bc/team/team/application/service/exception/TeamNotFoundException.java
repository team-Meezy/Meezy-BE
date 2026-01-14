package com.example.meezy.bc.team.team.application.service.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class TeamNotFoundException extends CustomException {

    public TeamNotFoundException() {
        super(ErrorCode.TEAM_NOT_FOUND);
    }
}
