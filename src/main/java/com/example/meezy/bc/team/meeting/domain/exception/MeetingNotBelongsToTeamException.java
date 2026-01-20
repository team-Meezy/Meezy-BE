package com.example.meezy.bc.team.meeting.domain.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class MeetingNotBelongsToTeamException extends CustomException {

    public MeetingNotBelongsToTeamException() {
        super(ErrorCode.MEETING_NOT_BELONGS_TO_TEAM);
    }
}
