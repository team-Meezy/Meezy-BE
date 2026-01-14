package com.example.meezy.bc.team.team.domain.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class NotTeamLeaderException extends CustomException {

    public NotTeamLeaderException() {
        super(ErrorCode.NOT_TEAM_LEADER);
    }
}
