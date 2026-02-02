package com.example.meezy.bc.collaboration.participation_metrics.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ParticipationRate {

    private static final double VOICE_WEIGHT = 0.5;
    private static final double CONNECTION_WEIGHT = 0.49;
    private static final double CHAT_WEIGHT = 0.01;

    @Column(name = "participation_rate")
    private double value;

    public static ParticipationRate calculate(
            int voiceCount,
            int maxVoiceCount,
            long connectionSeconds,
            long meetingSeconds,
            int chatCount,
            int maxChatCount
    ) {
        double voiceRate = calculateRatio(voiceCount, maxVoiceCount);
        double connectionRate = calculateRatio(connectionSeconds, meetingSeconds);
        double chatRate = calculateRatio(chatCount, maxChatCount);

        double rate = (voiceRate * VOICE_WEIGHT)
                + (connectionRate * CONNECTION_WEIGHT)
                + (chatRate * CHAT_WEIGHT);

        return new ParticipationRate(Math.min(rate, 1.0));
    }

    public static ParticipationRate zero() {
        return new ParticipationRate(0.0);
    }

    private static double calculateRatio(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0.0;
        }
        return (double) numerator / denominator;
    }
}
