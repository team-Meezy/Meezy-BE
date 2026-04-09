package com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class PreparedAudio implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(PreparedAudio.class);

    private final Path workingDirectory;
    private final List<Path> chunkPaths;

    private PreparedAudio(Path workingDirectory, List<Path> chunkPaths) {
        this.workingDirectory = workingDirectory;
        this.chunkPaths = List.copyOf(chunkPaths);
    }

    public static PreparedAudio direct(Path chunkPath) {
        return new PreparedAudio(null, List.of(chunkPath));
    }

    public static PreparedAudio temporary(Path workingDirectory, List<Path> chunkPaths) {
        return new PreparedAudio(workingDirectory, chunkPaths);
    }

    public List<Path> chunkPaths() {
        return chunkPaths;
    }

    @Override
    public void close() {
        if (workingDirectory == null) {
            return;
        }

        try (var paths = Files.walk(workingDirectory)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(this::deleteQuietly);
        } catch (IOException e) {
            log.warn("임시 오디오 작업 디렉터리 삭제 실패: {}", workingDirectory, e);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("임시 오디오 파일 삭제 실패: {}", path, e);
        }
    }
}
