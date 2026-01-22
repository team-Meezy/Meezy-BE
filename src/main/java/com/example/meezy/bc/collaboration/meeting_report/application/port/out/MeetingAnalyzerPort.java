package com.example.meezy.bc.collaboration.meeting_report.application.port.out;

public interface MeetingAnalyzerPort {

    String transcribe(byte[] audioData, String originalFilename, String contentType);

    String generateSummary(String transcript);

    String generateFeedback(String transcript);
}
