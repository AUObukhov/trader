package ru.obukhov.trader.trading.strategy.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.util.stream.Stream;

class GoldenCrossStrategyParamsValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final TestParams params = new TestParams(0.1f, 1, 0.6f, false);

        AssertUtils.assertNoViolations(params);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forValidationFails() {
        return Stream.of(
                Arguments.of(
                        new TestParams(null, 1, 0.6f, false),
                        "minimumProfit is mandatory"
                ),
                Arguments.of(
                        new TestParams(-0.1f, 1, 0.6f, false),
                        "minimumProfit min value is 0"
                ),

                Arguments.of(
                        new TestParams(0.1f, null, 0.6f, false),
                        "order is mandatory"
                ),
                Arguments.of(
                        new TestParams(0.1f, -1, 0.6f, false),
                        "order min value is 1"
                ),
                Arguments.of(
                        new TestParams(0.1f, 0, 0.6f, false),
                        "order min value is 1"
                ),

                Arguments.of(
                        new TestParams(0.1f, 1, null, false),
                        "indexCoefficient is mandatory"
                ),
                Arguments.of(
                        new TestParams(0.1f, 1, -0.1f, false),
                        "indexCoefficient min value is 0"
                ),
                Arguments.of(
                        new TestParams(0.1f, 1, 1.1f, false),
                        "indexCoefficient max value is 1"
                ),

                Arguments.of(
                        new TestParams(0.3f, 1, 0.6f, null),
                        "greedy is mandatory"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forValidationFails")
    void validationFails(TestParams params, String expectedMessage) {
        AssertUtils.assertViolation(params, expectedMessage);
    }

    private static final class TestParams extends GoldenCrossStrategyParams {
        public TestParams(
                final Float minimumProfit,
                final Integer order,
                final Float indexCoefficient,
                final Boolean greedy
        ) {
            super(minimumProfit, order, indexCoefficient, greedy);
        }
    }
}
