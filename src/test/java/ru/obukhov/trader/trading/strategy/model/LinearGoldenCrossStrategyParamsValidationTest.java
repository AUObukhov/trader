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

class LinearGoldenCrossStrategyParamsValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final LinearGoldenCrossStrategyParams params =
                new LinearGoldenCrossStrategyParams(0.6f, false, 3, 6);

        Set<ConstraintViolation<Object>> violations = validator.validate(params);

        Assertions.assertTrue(violations.isEmpty());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forValidationFails() {
        return Stream.of(
                Arguments.of(
                        new LinearGoldenCrossStrategyParams(0.6f, false, 7, 6),
                        "smallWindow must lower than bigWindow"
                ),
                Arguments.of(
                        new LinearGoldenCrossStrategyParams(0.6f, false, null, 6),
                        "smallWindow is mandatory"
                ),
                Arguments.of(
                        new LinearGoldenCrossStrategyParams(0.6f, false, -1, 6),
                        "smallWindow min value is 1"
                ),
                Arguments.of(
                        new LinearGoldenCrossStrategyParams(0.6f, false, 0, 6),
                        "smallWindow min value is 1"
                ),
                Arguments.of(
                        new LinearGoldenCrossStrategyParams(0.6f, false, 3, null),
                        "bigWindow is mandatory"
                ),
                Arguments.of(
                        new LinearGoldenCrossStrategyParams(null, false, 3, 6),
                        "indexCoefficient is mandatory"
                ),
                Arguments.of(
                        new LinearGoldenCrossStrategyParams(-0.1f, false, 3, 6),
                        "indexCoefficient min value is 0"
                ),
                Arguments.of(
                        new LinearGoldenCrossStrategyParams(1.1f, false, 3, 6),
                        "indexCoefficient max value is 1"
                ),
                Arguments.of(
                        new LinearGoldenCrossStrategyParams(0.6f, null, 3, 6),
                        "greedy is mandatory"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forValidationFails")
    void validationFails(LinearGoldenCrossStrategyParams params, String expectedMessage) {
        final Set<ConstraintViolation<Object>> violations = validator.validate(params);

        AssertUtils.assertViolation(violations, expectedMessage);
    }

}