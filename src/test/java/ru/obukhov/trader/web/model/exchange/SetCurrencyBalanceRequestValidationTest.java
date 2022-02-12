package ru.obukhov.trader.web.model.exchange;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.math.BigDecimal;

class SetCurrencyBalanceRequestValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final SetCurrencyBalanceRequest request = new SetCurrencyBalanceRequest();
        request.setCurrency(Currency.RUB);
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
        request.setCurrency(Currency.RUB);
        request.setBalance(null);

        AssertUtils.assertViolation(request, "balance is mandatory");
    }

}