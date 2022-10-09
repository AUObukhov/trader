package ru.obukhov.trader.web.model.exchange;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.test.utils.AssertUtils;

class SetCurrencyBalanceRequestValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final SetCurrencyBalanceRequest request = new SetCurrencyBalanceRequest();
        request.setCurrency(Currency.RUB);
        request.setBalance(DecimalUtils.setDefaultScale(10));

        AssertUtils.assertNoViolations(request);
    }

    @Test
    void validationFails_whenCurrencyIsNull() {
        final SetCurrencyBalanceRequest request = new SetCurrencyBalanceRequest();
        request.setCurrency(null);
        request.setBalance(DecimalUtils.setDefaultScale(10));

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