package ru.obukhov.trader.trading.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.util.stream.Stream;

class CrossStrategyParamsValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final CrossStrategyParams params = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );

        AssertUtils.assertNoViolations(params);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forValidationFails() {
        return Stream.of(
                Arguments.of(
                        new CrossStrategyParams(null, 1, 0.6f, false, 3, 5),
                        "minimumProfit is mandatory"
                ),

                Arguments.of(
                        new CrossStrategyParams(0.1f, null, 0.6f, false, 3, 5),
                        "order is mandatory"
                ),
                Arguments.of(
                        new CrossStrategyParams(0.1f, -1, 0.6f, false, 3, 5),
                        "order min value is 1"
                ),
                Arguments.of(
                        new CrossStrategyParams(0.1f, 0, 0.6f, false, 3, 5),
                        "order min value is 1"
                ),

                Arguments.of(
                        new CrossStrategyParams(0.1f, 1, null, false, 3, 5),
                        "indexCoefficient is mandatory"
                ),
                Arguments.of(
                        new CrossStrategyParams(0.1f, 1, -0.1f, false, 3, 5),
                        "indexCoefficient min value is 0"
                ),
                Arguments.of(
                        new CrossStrategyParams(0.1f, 1, 1.1f, false, 3, 5),
                        "indexCoefficient max value is 1"
                ),

                Arguments.of(
                        new CrossStrategyParams(0.3f, 1, 0.6f, null, 3, 5),
                        "greedy is mandatory"
                ),

                Arguments.of(
                        new CrossStrategyParams(0.1f, 1, 0.6f, false, 7, 6),
                        "smallWindow must lower than bigWindow"
                ),
                Arguments.of(
                        new CrossStrategyParams(0.1f, 1, 0.6f, false, null, 6),
                        "smallWindow is mandatory"
                ),
                Arguments.of(
                        new CrossStrategyParams(0.1f, 1, 0.6f, false, -1, 6),
                        "smallWindow must be positive"
                ),
                Arguments.of(
                        new CrossStrategyParams(0.1f, 1, 0.6f, false, 0, 6),
                        "smallWindow must be positive"
                ),

                Arguments.of(
                        new CrossStrategyParams(0.1f, 1, 0.6f, false, 3, null),
                        "bigWindow is mandatory"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forValidationFails")
    void validationFails(CrossStrategyParams params, String expectedMessage) {
        AssertUtils.assertViolation(params, expectedMessage);
    }

}
