package ru.obukhov.trader.config;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

class TradingConfigValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final TradingConfig tradingConfig = new TradingConfig(CandleResolution._1MIN, StrategyType.CONSERVATIVE);

        AssertUtils.assertNoViolations(tradingConfig);
    }

    @Test
    void validationFails_whenCandleResolutionIsNull() {
        final TradingConfig tradingConfig = new TradingConfig(null, StrategyType.CONSERVATIVE);

        AssertUtils.assertViolation(tradingConfig, "candleResolution is mandatory");
    }

    @Test
    void validationFails_whenStrategyTypeIsNull() {
        final TradingConfig tradingConfig = new TradingConfig(CandleResolution._1MIN, null);

        AssertUtils.assertViolation(tradingConfig, "strategyType is mandatory");
    }

}