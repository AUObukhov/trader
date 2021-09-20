package ru.obukhov.trader.web.model;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

class TradingConfigValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final TradingConfig tradingConfig = new TradingConfig(null, "ticker", CandleResolution._1MIN, 0.003, StrategyType.CONSERVATIVE);

        AssertUtils.assertNoViolations(tradingConfig);
    }

    @Test
    void validationFails_whenTickerIsNull() {
        final TradingConfig tradingConfig = new TradingConfig(null, null, CandleResolution._1MIN, 0.003, StrategyType.CONSERVATIVE);

        AssertUtils.assertViolation(tradingConfig, "ticker is mandatory");
    }

    @Test
    void validationFails_whenCandleResolutionIsNull() {
        final TradingConfig tradingConfig = new TradingConfig(null, "ticker", null, 0.003, StrategyType.CONSERVATIVE);

        AssertUtils.assertViolation(tradingConfig, "candleResolution is mandatory");
    }

    @Test
    void validationFails_whenCommissionIsNull() {
        final TradingConfig tradingConfig = new TradingConfig(null, "ticker", CandleResolution._1MIN, null, StrategyType.CONSERVATIVE);

        AssertUtils.assertViolation(tradingConfig, "commission is mandatory");
    }

    @Test
    void validationFails_whenStrategyTypeIsNull() {
        final TradingConfig tradingConfig = new TradingConfig(null, "ticker", CandleResolution._1MIN, 0.003, null);

        AssertUtils.assertViolation(tradingConfig, "strategyType is mandatory");
    }

}