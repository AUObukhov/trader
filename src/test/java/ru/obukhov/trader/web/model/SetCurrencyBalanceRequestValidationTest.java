package ru.obukhov.trader.web.model;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.web.model.exchange.SetCurrencyBalanceRequest;
import ru.tinkoff.invest.openapi.model.rest.SandboxCurrency;

import java.math.BigDecimal;

class SetCurrencyBalanceRequestValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final SetCurrencyBalanceRequest request = new SetCurrencyBalanceRequest();
        request.setCurrency(SandboxCurrency.RUB);
        request.setBalance(BigDecimal.TEN);

        AssertUtils.assertNoViolations(request);
    }

    @Test
    void validationFails_whenCurrencyIsNull() {
        final SetCurrencyBalanceRequest request = new SetCurrencyBalanceRequest();
        request.setCurrency(null);
        request.setBalance(BigDecimal.TEN);

        AssertUtils.assertViolation(request, "currency is mandatory");
    }

    @Test
    void validationFails_whenBalanceIsNull() {
        final SetCurrencyBalanceRequest request = new SetCurrencyBalanceRequest();
        request.setCurrency(SandboxCurrency.RUB);
        request.setBalance(null);

        AssertUtils.assertViolation(request, "balance is mandatory");
    }

}