package ru.obukhov.trader.web.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.web.model.exchange.SetPositionBalanceRequest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.util.Set;

class SetPositionBalanceRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final SetPositionBalanceRequest request = new SetPositionBalanceRequest();
        request.setTicker("ticker");
        request.setBalance(BigDecimal.TEN);

        final Set<ConstraintViolation<SetPositionBalanceRequest>> violations = validator.validate(request);
        Assertions.assertTrue(violations.isEmpty());
    }

    @Test
    void validationFails_whenTickerIsNull() {
        final SetPositionBalanceRequest request = new SetPositionBalanceRequest();
        request.setTicker(null);
        request.setBalance(BigDecimal.TEN);

        final Set<ConstraintViolation<SetPositionBalanceRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "ticker is mandatory");
    }

    @Test
    void validationFails_whenTickerIsEmpty() {
        final SetPositionBalanceRequest request = new SetPositionBalanceRequest();
        request.setTicker(null);
        request.setBalance(BigDecimal.TEN);

        final Set<ConstraintViolation<SetPositionBalanceRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "ticker is mandatory");
    }

    @Test
    void validationFails_whenBalanceIsNull() {
        final SetPositionBalanceRequest request = new SetPositionBalanceRequest();
        request.setTicker("ticker");
        request.setBalance(null);

        final Set<ConstraintViolation<SetPositionBalanceRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "balance is mandatory");
    }

}