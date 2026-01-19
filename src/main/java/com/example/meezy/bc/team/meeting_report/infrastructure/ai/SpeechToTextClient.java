package com.example.meezy.bc.team.meeting_report.infrastructure.ai;

import com.example.meezy.bc.team.meeting.application.port.out.TranscriptionPort;
import com.example.meezy.bc.team.meeting_report.infrastructure.ai.exception.AudioConversionException;
import com.example.meezy.bc.team.meeting_report.infrastructure.ai.exception.EmptyAudioFileException;
import com.example.meezy.bc.team.meeting_report.infrastructure.ai.exception.InvalidAudioContentTypeException;
import com.example.meezy.bc.team.meeting_report.infrastructure.ai.exception.InvalidAudioExtensionException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SpeechToTextClient implements TranscriptionPort {

    private final OpenAiAudioTranscriptionModel transcriptionModel;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("mp3", "wav");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("audio/mpeg", "audio/wav", "audio/wave");

    @Override
    public String transcribe(MultipartFile audio) {
        validateNotEmpty(audio);
        validateExtension(audio.getOriginalFilename());
        validateContentType(audio.getContentType());

        Resource resource = toResource(audio);
        return transcriptionModel.call(resource);
    }

    private void validateNotEmpty(MultipartFile audio) {
        if (audio == null || audio.isEmpty()) {
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

    private Resource toResource(MultipartFile audio) {
        try {
            return new InputStreamResource(audio.getInputStream());
        } catch (IOException e) {
            throw new AudioConversionException();
        }
    }
}
