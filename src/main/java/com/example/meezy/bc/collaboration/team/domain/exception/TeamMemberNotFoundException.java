package com.example.meezy.bc.collaboration.team.domain.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class TeamMemberNotFoundException extends CustomException {

    public TeamMemberNotFoundException() {
        super(ErrorCode.TEAM_MEMBER_NOT_FOUND);
    }
}
