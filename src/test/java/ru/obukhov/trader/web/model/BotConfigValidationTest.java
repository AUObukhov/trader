package ru.obukhov.trader.web.model;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

class BotConfigValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final BotConfig botConfig = new BotConfig(null, "ticker", CandleResolution._1MIN, 0.003, StrategyType.CONSERVATIVE);

        AssertUtils.assertNoViolations(botConfig);
    }

    @Test
    void validationFails_whenTickerIsNull() {
        final BotConfig botConfig = new BotConfig(null, null, CandleResolution._1MIN, 0.003, StrategyType.CONSERVATIVE);

        AssertUtils.assertViolation(botConfig, "ticker is mandatory");
    }

    @Test
    void validationFails_whenCandleResolutionIsNull() {
        final BotConfig botConfig = new BotConfig(null, "ticker", null, 0.003, StrategyType.CONSERVATIVE);

        AssertUtils.assertViolation(botConfig, "candleResolution is mandatory");
    }

    @Test
    void validationFails_whenCommissionIsNull() {
        final BotConfig botConfig = new BotConfig(null, "ticker", CandleResolution._1MIN, null, StrategyType.CONSERVATIVE);

        AssertUtils.assertViolation(botConfig, "commission is mandatory");
    }

    @Test
    void validationFails_whenStrategyTypeIsNull() {
        final BotConfig botConfig = new BotConfig(null, "ticker", CandleResolution._1MIN, 0.003, null);

        AssertUtils.assertViolation(botConfig, "strategyType is mandatory");
    }

}