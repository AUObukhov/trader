package ru.obukhov.trader.trading.strategy.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.util.stream.Stream;

class ExponentialGoldenCrossStrategyParamsValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final ExponentialGoldenCrossStrategyParams params =
                new ExponentialGoldenCrossStrategyParams(0.1f, 0.6f, false, 0.6, 0.3);

        AssertUtils.assertNoViolations(params);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forValidationFails() {
        return Stream.of(
                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(null, 0.6f, false, 0.6, 0.3),
                        "minimumProfit is mandatory"
                ),
                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(-0.1f, 0.6f, false, 0.6, 0.3),
                        "minimumProfit min value is 0"
                ),

                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(0.1f, null, false, 0.6, 0.3),
                        "indexCoefficient is mandatory"
                ),
                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(0.1f, -0.1f, false, 0.6, 0.3),
                        "indexCoefficient min value is 0"
                ),
                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(0.1f, 1.1f, false, 0.6, 0.3),
                        "indexCoefficient max value is 1"
                ),

                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(0.1f, 0.6f, null, 0.6, 0.3),
                        "greedy is mandatory"
                ),

                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(0.1f, 0.6f, false, null, 0.3),
                        "fastWeightDecrease is mandatory"
                ),
                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(0.1f, 0.6f, false, 1.1, 0.3),
                        "fastWeightDecrease max value is 1"
                ),

                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(0.1f, 0.6f, false, 0.3, 0.6),
                        "slowWeightDecrease must lower than fastWeightDecrease"
                ),
                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(0.1f, 0.6f, false, 0.6, null),
                        "slowWeightDecrease is mandatory"
                ),
                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(0.1f, 0.6f, false, 0.3, -0.1),
                        "slowWeightDecrease min value is 0"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forValidationFails")
    void validationFails(final ExponentialGoldenCrossStrategyParams params, final String expectedMessage) {
        AssertUtils.assertViolation(params, expectedMessage);
    }

}