package com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpeechToTextClient {

    private final OpenAiAudioTranscriptionModel transcriptionModel;

    public String transcribe(byte[] audioChunk) {
        Resource resource = toResource(audioChunk);
        return transcriptionModel.call(resource);
    }

    private Resource toResource(byte[] audioData) {
        return new ByteArrayResource(audioData) {
            @Override
            public String getFilename() {
                return "audio.mp3";
            }
        };
    }
}
