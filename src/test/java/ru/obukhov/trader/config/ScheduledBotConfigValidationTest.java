package ru.obukhov.trader.config;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

class ScheduledBotConfigValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final ScheduledBotConfig botConfig = new ScheduledBotConfig();
        botConfig.setCandleResolution(CandleResolution._1MIN);
        botConfig.setStrategyType(StrategyType.CONSERVATIVE);

        AssertUtils.assertNoViolations(botConfig);
    }

    @Test
    void validationFails_whenCandleResolutionIsNull() {
        final ScheduledBotConfig botConfig = new ScheduledBotConfig();
        botConfig.setStrategyType(StrategyType.CONSERVATIVE);

        AssertUtils.assertViolation(botConfig, "candleResolution is mandatory");
    }

    @Test
    void validationFails_whenStrategyTypeIsNull() {
        final ScheduledBotConfig botConfig = new ScheduledBotConfig();
        botConfig.setCandleResolution(CandleResolution._1MIN);

        AssertUtils.assertViolation(botConfig, "strategyType is mandatory");
    }

}