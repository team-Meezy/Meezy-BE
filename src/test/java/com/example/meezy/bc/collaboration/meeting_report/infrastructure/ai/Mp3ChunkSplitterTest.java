package com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Mp3ChunkSplitter 테스트")
class Mp3ChunkSplitterTest {

    private final Mp3ChunkSplitter splitter = new Mp3ChunkSplitter();

    @Test
    @DisplayName("24MB 이하 파일은 분할하지 않는다")
    void does_not_split_small_file() {
        byte[] smallData = new byte[1024];

        List<byte[]> chunks = splitter.split(smallData);

        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0)).hasSize(1024);
    }

    @Test
    @DisplayName("24MB 초과 파일은 여러 청크로 분할된다")
    void splits_large_file() {
        int size = 24 * 1024 * 1024 + 1000;
        byte[] largeData = new byte[size];

        List<byte[]> chunks = splitter.split(largeData);

        assertThat(chunks).hasSizeGreaterThan(1);

        int totalSize = chunks.stream().mapToInt(c -> c.length).sum();
        assertThat(totalSize).isEqualTo(size);
    }

    @Test
    @DisplayName("MP3 프레임 sync 경계에서 분할한다")
    void splits_at_mp3_frame_sync() {
        int size = 24 * 1024 * 1024 + 1000;
        byte[] data = new byte[size];

        // MP3 frame sync marker (0xFF 0xFB) near the chunk boundary
        int syncPosition = 24 * 1024 * 1024 - 100;
        data[syncPosition] = (byte) 0xFF;
        data[syncPosition + 1] = (byte) 0xFB;

        List<byte[]> chunks = splitter.split(data);

        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks.get(0).length).isEqualTo(syncPosition);
    }

    @Test
    @DisplayName("정확히 24MB 파일은 분할하지 않는다")
    void does_not_split_exact_24mb() {
        byte[] data = new byte[24 * 1024 * 1024];

        List<byte[]> chunks = splitter.split(data);

        assertThat(chunks).hasSize(1);
    }

    @Test
    @DisplayName("모든 청크의 합은 원본 크기와 같다")
    void total_size_equals_original() {
        int size = 50 * 1024 * 1024;
        byte[] data = new byte[size];

        // Place some frame syncs
        for (int i = 0; i < size - 1; i += 1000) {
            data[i] = (byte) 0xFF;
            data[i + 1] = (byte) 0xFB;
        }

        List<byte[]> chunks = splitter.split(data);

        int totalSize = chunks.stream().mapToInt(c -> c.length).sum();
        assertThat(totalSize).isEqualTo(size);
    }
}
