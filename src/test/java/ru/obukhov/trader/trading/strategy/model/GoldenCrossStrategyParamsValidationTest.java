package ru.obukhov.trader.trading.strategy.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Stream;

class GoldenCrossStrategyParamsValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final TestParams params = new TestParams(0.1f, 0.6f, false);

        final Set<ConstraintViolation<Object>> violations = validator.validate(params);

        Assertions.assertTrue(violations.isEmpty());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forValidationFails() {
        return Stream.of(
                Arguments.of(
                        new TestParams(null, 0.6f, false),
                        "minimumProfit is mandatory"
                ),
                Arguments.of(
                        new TestParams(-0.1f, 0.6f, false),
                        "minimumProfit min value is 0"
                ),

                Arguments.of(
                        new TestParams(0.1f, null, false),
                        "indexCoefficient is mandatory"
                ),
                Arguments.of(
                        new TestParams(0.1f, -0.1f, false),
                        "indexCoefficient min value is 0"
                ),
                Arguments.of(
                        new TestParams(0.1f, 1.1f, false),
                        "indexCoefficient max value is 1"
                ),

                Arguments.of(
                        new TestParams(0.3f, 0.6f, null),
                        "greedy is mandatory"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forValidationFails")
    void validationFails(TestParams params, String expectedMessage) {
        final Set<ConstraintViolation<Object>> violations = validator.validate(params);

        AssertUtils.assertViolation(violations, expectedMessage);
    }

    private static final class TestParams extends GoldenCrossStrategyParams {
        public TestParams(
                final Float minimumProfit,
                final Float indexCoefficient,
                final Boolean greedy
        ) {
            super(minimumProfit, indexCoefficient, greedy);
        }
    }
}
