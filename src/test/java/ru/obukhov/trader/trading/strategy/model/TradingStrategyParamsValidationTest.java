package ru.obukhov.trader.trading.strategy.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

class TradingStrategyParamsValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final SimpleGoldenCrossStrategyParams params =
                new SimpleGoldenCrossStrategyParams(0.1f, 0.6f, false, 3, 6);

        final Set<ConstraintViolation<Object>> violations = validator.validate(params);

        Assertions.assertTrue(violations.isEmpty());
    }

    @Test
    void validationFails_whenMinimumProfitIsNull() {
        final TradingStrategyParams params = new TradingStrategyParams();

        final Set<ConstraintViolation<Object>> violations = validator.validate(params);

        AssertUtils.assertViolation(violations, "minimumProfit is mandatory");
    }

}