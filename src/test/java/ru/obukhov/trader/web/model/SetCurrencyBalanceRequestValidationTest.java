package ru.obukhov.trader.web.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.web.model.exchange.SetCurrencyBalanceRequest;
import ru.tinkoff.invest.openapi.models.Currency;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.util.Set;

class SetCurrencyBalanceRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        SetCurrencyBalanceRequest request = new SetCurrencyBalanceRequest();
        request.setCurrency(Currency.RUB);
        request.setBalance(BigDecimal.TEN);

        Set<ConstraintViolation<SetCurrencyBalanceRequest>> violations = validator.validate(request);
        Assertions.assertTrue(violations.isEmpty());
    }

    @Test
    void validationFails_whenCurrencyIsNull() {
        SetCurrencyBalanceRequest request = new SetCurrencyBalanceRequest();
        request.setCurrency(null);
        request.setBalance(BigDecimal.TEN);

        Set<ConstraintViolation<SetCurrencyBalanceRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "currency is mandatory");
    }

    @Test
    void validationFails_whenBalanceIsNull() {
        SetCurrencyBalanceRequest request = new SetCurrencyBalanceRequest();
        request.setCurrency(Currency.RUB);
        request.setBalance(null);

        Set<ConstraintViolation<SetCurrencyBalanceRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "balance is mandatory");
    }

}