package ru.obukhov.trader.trading.strategy.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.util.stream.Stream;

class LinearGoldenCrossStrategyParamsValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final LinearGoldenCrossStrategyParams params = new LinearGoldenCrossStrategyParams(
                0.1f,
                0.6f,
                false,
                3,
                6
        );

        AssertUtils.assertNoViolations(params);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forValidationFails() {
        return Stream.of(
                Arguments.of(
                        new LinearGoldenCrossStrategyParams(null, 0.6f, false, 3, 6),
                        "minimumProfit is mandatory"
                ),
                Arguments.of(
                        new LinearGoldenCrossStrategyParams(-0.1f, 0.6f, false, 3, 6),
                        "minimumProfit min value is 0"
                ),

                Arguments.of(
                        new LinearGoldenCrossStrategyParams(0.1f, null, false, 3, 6),
                        "indexCoefficient is mandatory"
                ),
                Arguments.of(
                        new LinearGoldenCrossStrategyParams(0.1f, -0.1f, false, 3, 6),
                        "indexCoefficient min value is 0"
                ),
                Arguments.of(
                        new LinearGoldenCrossStrategyParams(0.1f, 1.1f, false, 3, 6),
                        "indexCoefficient max value is 1"
                ),

                Arguments.of(
                        new LinearGoldenCrossStrategyParams(0.1f, 0.6f, null, 3, 6),
                        "greedy is mandatory"
                ),

                Arguments.of(
                        new LinearGoldenCrossStrategyParams(0.1f, 0.6f, false, 7, 6),
                        "smallWindow must lower than bigWindow"
                ),
                Arguments.of(
                        new LinearGoldenCrossStrategyParams(0.1f, 0.6f, false, null, 6),
                        "smallWindow is mandatory"
                ),
                Arguments.of(
                        new LinearGoldenCrossStrategyParams(0.1f, 0.6f, false, -1, 6),
                        "smallWindow min value is 1"
                ),
                Arguments.of(
                        new LinearGoldenCrossStrategyParams(0.1f, 0.6f, false, 0, 6),
                        "smallWindow min value is 1"
                ),
                Arguments.of(
                        new LinearGoldenCrossStrategyParams(0.1f, 0.6f, false, 3, null),
                        "bigWindow is mandatory"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forValidationFails")
    void validationFails(final LinearGoldenCrossStrategyParams params, final String expectedMessage) {
        AssertUtils.assertViolation(params, expectedMessage);
    }

}