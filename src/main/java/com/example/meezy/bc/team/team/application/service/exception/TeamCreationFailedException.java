package com.example.meezy.bc.team.team.application.service.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class TeamCreationFailedException extends CustomException {

    public TeamCreationFailedException() {
        super(ErrorCode.TEAM_CREATION_FAILED);
    }
}
