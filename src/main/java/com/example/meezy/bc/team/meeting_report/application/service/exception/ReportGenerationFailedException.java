package com.example.meezy.bc.team.meeting_report.application.service.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class ReportGenerationFailedException extends CustomException {

    public ReportGenerationFailedException() {
        super(ErrorCode.REPORT_GENERATION_FAILED);
    }
}
