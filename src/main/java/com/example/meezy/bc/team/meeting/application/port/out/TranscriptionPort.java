package com.example.meezy.bc.team.meeting.application.port.out;

import org.springframework.web.multipart.MultipartFile;

public interface TranscriptionPort {

    String transcribe(MultipartFile audio);
}
