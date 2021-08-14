package ru.obukhov.trader.web.model.exchange;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.util.List;

class SetTickersRequestValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final SetTickersRequest request = new SetTickersRequest();
        request.setTickers(List.of("BABA"));

        AssertUtils.assertNoViolations(request);
    }

    @Test
    void validationFails_whenTickerIsNull() {
        final SetTickersRequest request = new SetTickersRequest();
        request.setTickers(null);

        AssertUtils.assertViolation(request, "tickers are mandatory");
    }

    @Test
    void validationFails_whenTickerIsEmpty() {
        final SetTickersRequest request = new SetTickersRequest();
        request.setTickers(List.of());

        AssertUtils.assertViolation(request, "tickers are mandatory");
    }

}