package ru.obukhov.trader.trading.model;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;

class TradingStrategyParamsValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final GoldenCrossStrategyParams params = new GoldenCrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                6
        );

        AssertUtils.assertNoViolations(params);
    }

    @Test
    void validationFails_whenMinimumProfitIsNull() {
        final TradingStrategyParams params = new TradingStrategyParams();

        AssertUtils.assertViolation(params, "minimumProfit is mandatory");
    }

}