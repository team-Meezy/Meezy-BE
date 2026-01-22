package com.example.meezy.bc.collaboration.team.domain.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class DuplicateTeamMemberException extends CustomException {

    public DuplicateTeamMemberException() {
        super(ErrorCode.DUPLICATE_TEAM_MEMBER);
    }
}
