package com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai;

import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.exception.EmptyAudioFileException;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.exception.InvalidAudioContentTypeException;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.exception.InvalidAudioExtensionException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class SpeechToTextClient {

    private final OpenAiAudioTranscriptionModel transcriptionModel;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("mp3", "wav");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("audio/mpeg", "audio/wav", "audio/wave");

    public String transcribe(byte[] audioData, String originalFilename, String contentType) {
        validateNotEmpty(audioData);
        validateExtension(originalFilename);
        validateContentType(contentType);

        Resource resource = toResource(audioData, originalFilename);
        return transcriptionModel.call(resource);
    }

    private void validateNotEmpty(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            throw new EmptyAudioFileException();
        }
    }

    private void validateExtension(String filename) {
        if (filename == null || !hasAllowedExtension(filename)) {
            throw new InvalidAudioExtensionException();
        }
    }

    private void validateContentType(String contentType) {
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidAudioContentTypeException();
        }
    }

    private boolean hasAllowedExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return false;
        }
        String extension = filename.substring(lastDotIndex + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    private Resource toResource(byte[] audioData, String filename) {
        return new ByteArrayResource(audioData) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }
}
