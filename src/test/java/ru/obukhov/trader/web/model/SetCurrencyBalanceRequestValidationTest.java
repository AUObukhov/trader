package ru.obukhov.trader.web.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.web.model.exchange.SetCurrencyBalanceRequest;
import ru.tinkoff.invest.openapi.model.rest.SandboxCurrency;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.util.Set;

class SetCurrencyBalanceRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final SetCurrencyBalanceRequest request = new SetCurrencyBalanceRequest();
        request.setCurrency(SandboxCurrency.RUB);
        request.setBalance(BigDecimal.TEN);

        final Set<ConstraintViolation<SetCurrencyBalanceRequest>> violations = validator.validate(request);
        Assertions.assertTrue(violations.isEmpty());
    }

    @Test
    void validationFails_whenCurrencyIsNull() {
        final SetCurrencyBalanceRequest request = new SetCurrencyBalanceRequest();
        request.setCurrency(null);
        request.setBalance(BigDecimal.TEN);

        final Set<ConstraintViolation<SetCurrencyBalanceRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "currency is mandatory");
    }

    @Test
    void validationFails_whenBalanceIsNull() {
        final SetCurrencyBalanceRequest request = new SetCurrencyBalanceRequest();
        request.setCurrency(SandboxCurrency.RUB);
        request.setBalance(null);

        final Set<ConstraintViolation<SetCurrencyBalanceRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "balance is mandatory");
    }

}