package com.example.meezy.bc.collaboration.participation_metrics.application.service.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class ParticipationMetricsNotFoundException extends CustomException {

    public ParticipationMetricsNotFoundException() {
        super(ErrorCode.PARTICIPATION_METRICS_NOT_FOUND);
    }
}
