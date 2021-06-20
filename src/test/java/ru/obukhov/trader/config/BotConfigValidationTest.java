package ru.obukhov.trader.config;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

class BotConfigValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final BotConfig botConfig = new BotConfig(CandleResolution._1MIN, StrategyType.CONSERVATIVE);

        AssertUtils.assertNoViolations(botConfig);
    }

    @Test
    void validationFails_whenCandleResolutionIsNull() {
        final BotConfig botConfig = new BotConfig(null, StrategyType.CONSERVATIVE);

        AssertUtils.assertViolation(botConfig, "candleResolution is mandatory");
    }

    @Test
    void validationFails_whenStrategyTypeIsNull() {
        final BotConfig botConfig = new BotConfig(CandleResolution._1MIN, null);

        AssertUtils.assertViolation(botConfig, "strategyType is mandatory");
    }

}