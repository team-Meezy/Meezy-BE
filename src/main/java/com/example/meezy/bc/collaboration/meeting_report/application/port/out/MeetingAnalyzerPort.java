package com.example.meezy.bc.collaboration.meeting_report.application.port.out;

public interface MeetingAnalyzerPort {

    String transcribe(String s3Key);

    String generateSummary(String transcript);

    String generateFeedback(String transcript);
}
