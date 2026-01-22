package com.example.meezy.bc.collaboration.meeting_report.application.service.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class MeetingReportNotFoundException extends CustomException {

    public MeetingReportNotFoundException() {
        super(ErrorCode.MEETING_REPORT_NOT_FOUND);
    }
}
