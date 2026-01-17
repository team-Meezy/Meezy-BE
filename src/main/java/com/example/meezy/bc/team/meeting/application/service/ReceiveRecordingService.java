package com.example.meezy.bc.team.meeting.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiveRecordingService {

    public void receive(UUID teamId, UUID meetingId, MultipartFile recording) {
        log.info("녹음 파일 수신: teamId={}, meetingId={}, fileName={}, size={}bytes",
                teamId, meetingId, recording.getOriginalFilename(), recording.getSize());

        // 현재는 저장하지 않고 수신만 수행
        // 추후 이벤트를 발행하여 AI에게 전달할 예정
    }
}
