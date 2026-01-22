package com.example.meezy.bc.collaboration.meeting.domain.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class NotTeamMemberException extends CustomException {

    public NotTeamMemberException() {
        super(ErrorCode.NOT_TEAM_MEMBER);
    }
}
