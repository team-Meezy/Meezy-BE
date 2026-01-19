package com.example.meezy.bc.team.meeting_report.application.port.out;

import org.springframework.web.multipart.MultipartFile;

public interface MeetingAnalyzerPort {

    String transcribe(MultipartFile audio);

    String generateSummary(String transcript);

    String generateFeedback(String transcript);
}
