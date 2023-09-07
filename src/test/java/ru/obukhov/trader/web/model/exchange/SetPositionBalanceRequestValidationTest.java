package ru.obukhov.trader.web.model.exchange;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.share.TestShares;

class SetPositionBalanceRequestValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final SetPositionBalanceRequest request = new SetPositionBalanceRequest();
        request.setFigi(TestShares.APPLE.share().figi());
        request.setBalance(DecimalUtils.setDefaultScale(10));

        AssertUtils.assertNoViolations(request);
    }

    @Test
    void validationFails_whenFigiIsNull() {
        final SetPositionBalanceRequest request = new SetPositionBalanceRequest();
        request.setFigi(null);
        request.setBalance(DecimalUtils.setDefaultScale(10));

        AssertUtils.assertViolation(request, "figi is mandatory");
    }

    @Test
    void validationFails_whenFigiIsEmpty() {
        final SetPositionBalanceRequest request = new SetPositionBalanceRequest();
        request.setFigi(null);
        request.setBalance(DecimalUtils.setDefaultScale(10));

        AssertUtils.assertViolation(request, "figi is mandatory");
    }

    @Test
    void validationFails_whenBalanceIsNull() {
        final SetPositionBalanceRequest request = new SetPositionBalanceRequest();
        request.setFigi(TestShares.APPLE.share().figi());
        request.setBalance(null);

        AssertUtils.assertViolation(request, "balance is mandatory");
    }

}