package com.example.meezy.bc.collaboration.meeting.infrastructure.adapter.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class SignalRateLimitExceededException extends CustomException {

    public SignalRateLimitExceededException() {
        super(ErrorCode.SIGNAL_RATE_LIMIT_EXCEEDED);
    }
}
