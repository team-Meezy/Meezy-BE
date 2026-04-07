package com.example.meezy.bc.collaboration.participation_metrics.domain;

import com.example.meezy.bc.collaboration.participation_metrics.domain.vo.ParticipationRate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("ParticipationRate VO 테스트")
class ParticipationRateTest {

    @Nested
    @DisplayName("참여율 계산")
    class CalculateTest {

        @Test
        @DisplayName("모든 지표가 최대일 때 참여율은 1.0이다")
        void calculate_returns_one_when_all_max() {
            ParticipationRate rate = ParticipationRate.calculate(
                    10, 10,    // voice: 100%
                    3600, 3600, // connection: 100%
                    5, 5        // chat: 100%
            );

            assertThat(rate.value()).isCloseTo(1.0, within(0.001));
        }

        @Test
        @DisplayName("가중치에 따라 참여율이 계산된다")
        void calculate_applies_weights_correctly() {
            // voice: 50%, connection: 100%, chat: 0%
            // 0.5 * 0.5 + 1.0 * 0.49 + 0.0 * 0.01 = 0.25 + 0.49 + 0 = 0.74
            ParticipationRate rate = ParticipationRate.calculate(
                    5, 10,
                    3600, 3600,
                    0, 10
            );

            assertThat(rate.value()).isCloseTo(0.74, within(0.001));
        }

        @Test
        @DisplayName("분모가 0이면 해당 비율은 0으로 계산된다")
        void calculate_handles_zero_denominator() {
            ParticipationRate rate = ParticipationRate.calculate(
                    10, 0,  // voice: max가 0이면 0%
                    0, 0,   // connection: max가 0이면 0%
                    0, 0    // chat: max가 0이면 0%
            );

            assertThat(rate.value()).isZero();
        }

        @Test
        @DisplayName("계산 결과가 1을 초과하면 1로 제한된다")
        void calculate_caps_at_one() {
            // 이론적으로는 1을 초과할 수 없지만 안전장치 테스트
            ParticipationRate rate = ParticipationRate.calculate(
                    100, 10,
                    10000, 1000,
                    100, 10
            );

            assertThat(rate.value()).isLessThanOrEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("zero 팩토리 메서드")
    class ZeroTest {

        @Test
        @DisplayName("zero는 0.0 값을 가진 ParticipationRate를 반환한다")
        void zero_returns_rate_with_zero_value() {
            ParticipationRate rate = ParticipationRate.zero();

            assertThat(rate.value()).isZero();
        }
    }
}
