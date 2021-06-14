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

class ExponentialGoldenCrossStrategyParamsValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final ExponentialGoldenCrossStrategyParams params =
                new ExponentialGoldenCrossStrategyParams(0.6, 0.3, 0.6f, false);

        Set<ConstraintViolation<Object>> violations = validator.validate(params);

        Assertions.assertTrue(violations.isEmpty());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forValidationFails() {
        return Stream.of(
                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(0.3, 0.6, 0.6f, false),
                        "slowWeightDecrease must lower than fastWeightDecrease"
                ),
                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(null, 0.3, 0.6f, false),
                        "fastWeightDecrease is mandatory"
                ),
                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(1.1, 0.3, 0.6f, false),
                        "fastWeightDecrease max value is 1"
                ),
                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(0.6, null, 0.6f, false),
                        "slowWeightDecrease is mandatory"
                ),
                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(0.3, -0.1, 0.6f, false),
                        "slowWeightDecrease min value is 0"
                ),
                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(0.6, 0.3, null, false),
                        "indexCoefficient is mandatory"
                ),
                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(0.6, 0.3, -0.1f, false),
                        "indexCoefficient min value is 0"
                ),
                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(0.6, 0.3, 1.1f, false),
                        "indexCoefficient max value is 1"
                ),
                Arguments.of(
                        new ExponentialGoldenCrossStrategyParams(0.6, 0.3, 0.6f, null),
                        "greedy is mandatory"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forValidationFails")
    void validationFails(ExponentialGoldenCrossStrategyParams params, String expectedMessage) {
        final Set<ConstraintViolation<Object>> violations = validator.validate(params);

        AssertUtils.assertViolation(violations, expectedMessage);
    }

}