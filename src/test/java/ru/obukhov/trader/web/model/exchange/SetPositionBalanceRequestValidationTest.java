package ru.obukhov.trader.web.model.exchange;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.share.TestShare1;

class SetPositionBalanceRequestValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final SetPositionBalanceRequest request = new SetPositionBalanceRequest();
        request.setTicker(TestShare1.TICKER);
        request.setBalance(DecimalUtils.setDefaultScale(10));

        AssertUtils.assertNoViolations(request);
    }

    @Test
    void validationFails_whenTickerIsNull() {
        final SetPositionBalanceRequest request = new SetPositionBalanceRequest();
        request.setTicker(null);
        request.setBalance(DecimalUtils.setDefaultScale(10));

        AssertUtils.assertViolation(request, "ticker is mandatory");
    }

    @Test
    void validationFails_whenTickerIsEmpty() {
        final SetPositionBalanceRequest request = new SetPositionBalanceRequest();
        request.setTicker(null);
        request.setBalance(DecimalUtils.setDefaultScale(10));

        AssertUtils.assertViolation(request, "ticker is mandatory");
    }

    @Test
    void validationFails_whenBalanceIsNull() {
        final SetPositionBalanceRequest request = new SetPositionBalanceRequest();
        request.setTicker(TestShare1.TICKER);
        request.setBalance(null);

        AssertUtils.assertViolation(request, "balance is mandatory");
    }

}