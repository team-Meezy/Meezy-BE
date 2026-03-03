package com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class Mp3ChunkSplitter {

    private static final int MAX_CHUNK_SIZE = 24 * 1024 * 1024; // 24MB

    public List<byte[]> split(byte[] audioData) {
        if (audioData.length <= MAX_CHUNK_SIZE) {
            return List.of(audioData);
        }

        List<byte[]> chunks = new ArrayList<>();
        int offset = 0;

        while (offset < audioData.length) {
            int end = Math.min(offset + MAX_CHUNK_SIZE, audioData.length);

            if (end < audioData.length) {
                int frameStart = findLastMp3FrameSync(audioData, offset, end);
                if (frameStart > offset) {
                    end = frameStart;
                }
            }

            chunks.add(Arrays.copyOfRange(audioData, offset, end));
            offset = end;
        }

        return chunks;
    }

    private int findLastMp3FrameSync(byte[] data, int searchFrom, int searchTo) {
        for (int i = searchTo - 1; i >= searchFrom + 1; i--) {
            if (isMp3FrameSync(data, i)) {
                return i;
            }
        }
        return searchFrom;
    }

    private boolean isMp3FrameSync(byte[] data, int offset) {
        if (offset + 1 >= data.length) {
            return false;
        }
        return (data[offset] & 0xFF) == 0xFF && ((data[offset + 1] & 0xE0) == 0xE0);
    }
}
