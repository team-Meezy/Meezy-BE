package com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai;

import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.exception.AudioConversionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class FfmpegAudioChunker {

    private static final long MAX_TRANSCRIPTION_BYTES = 24L * 1024 * 1024;
    private static final int NORMALIZED_BITRATE_KBPS = 64;
    private static final int NORMALIZED_SAMPLE_RATE = 16_000;
    private static final int NORMALIZED_CHANNELS = 1;
    private static final int SEGMENT_SECONDS = 40 * 60;
    private static final Pattern CHUNK_FILE_NAME = Pattern.compile("chunk-\\d{3}\\.mp3");

    public PreparedAudio prepare(Path sourceFile) {
        try {
            long sourceSize = Files.size(sourceFile);
            if (sourceSize <= MAX_TRANSCRIPTION_BYTES) {
                log.info("오디오 청크 분할 생략: sourceFile={}, audioBytes={}", sourceFile, sourceSize);
                return PreparedAudio.direct(sourceFile);
            }

            Path workingDirectory = Files.createTempDirectory("transcription-audio-");
            Path normalizedFile = workingDirectory.resolve("normalized.mp3");

            normalize(sourceFile, normalizedFile);
            long normalizedSize = Files.size(normalizedFile);
            if (normalizedSize <= MAX_TRANSCRIPTION_BYTES) {
                log.info(
                        "오디오 정규화 완료: sourceFile={}, normalizedBytes={}",
                        sourceFile,
                        normalizedSize
                );
                return PreparedAudio.temporary(workingDirectory, List.of(normalizedFile));
            }

            split(normalizedFile, workingDirectory);
            List<Path> chunkFiles = collectChunkFiles(workingDirectory);
            validateChunkSizes(chunkFiles);

            log.info(
                    "오디오 청크 분할 완료: sourceFile={}, normalizedBytes={}, chunkCount={}",
                    sourceFile,
                    normalizedSize,
                    chunkFiles.size()
            );
            return PreparedAudio.temporary(workingDirectory, chunkFiles);
        } catch (IOException e) {
            log.error("오디오 청크 준비 실패: sourceFile={}", sourceFile, e);
            throw new AudioConversionException();
        }
    }

    private void normalize(Path sourceFile, Path normalizedFile) {
        runFfmpeg(
                List.of(
                        "ffmpeg",
                        "-y",
                        "-hide_banner",
                        "-loglevel", "error",
                        "-i", sourceFile.toAbsolutePath().toString(),
                        "-vn",
                        "-ac", String.valueOf(NORMALIZED_CHANNELS),
                        "-ar", String.valueOf(NORMALIZED_SAMPLE_RATE),
                        "-b:a", NORMALIZED_BITRATE_KBPS + "k",
                        normalizedFile.toAbsolutePath().toString()
                ),
                "normalize"
        );
    }

    private void split(Path normalizedFile, Path workingDirectory) {
        Path outputPattern = workingDirectory.resolve("chunk-%03d.mp3");
        runFfmpeg(
                List.of(
                        "ffmpeg",
                        "-y",
                        "-hide_banner",
                        "-loglevel", "error",
                        "-i", normalizedFile.toAbsolutePath().toString(),
                        "-f", "segment",
                        "-segment_time", String.valueOf(SEGMENT_SECONDS),
                        "-c", "copy",
                        "-reset_timestamps", "1",
                        outputPattern.toAbsolutePath().toString()
                ),
                "split"
        );
    }

    private List<Path> collectChunkFiles(Path workingDirectory) throws IOException {
        try (var paths = Files.list(workingDirectory)) {
            List<Path> chunkFiles = paths
                    .filter(path -> CHUNK_FILE_NAME.matcher(path.getFileName().toString()).matches())
                    .sorted()
                    .toList();

            if (chunkFiles.isEmpty()) {
                throw new AudioConversionException();
            }
            return chunkFiles;
        }
    }

    private void validateChunkSizes(List<Path> chunkFiles) throws IOException {
        for (Path chunkFile : chunkFiles) {
            long chunkSize = Files.size(chunkFile);
            if (chunkSize > MAX_TRANSCRIPTION_BYTES) {
                log.error("오디오 청크 크기 초과: chunkFile={}, chunkBytes={}", chunkFile, chunkSize);
                throw new AudioConversionException();
            }
        }
    }

    private void runFfmpeg(List<String> command, String step) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            String output;
            try (var inputStream = process.getInputStream()) {
                output = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("ffmpeg {} 실패: exitCode={}, output={}", step, exitCode, output);
                throw new AudioConversionException();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("ffmpeg {} 인터럽트 발생", step, e);
            throw new AudioConversionException();
        } catch (IOException e) {
            log.error("ffmpeg {} 실행 실패", step, e);
            throw new AudioConversionException();
        }
    }
}
