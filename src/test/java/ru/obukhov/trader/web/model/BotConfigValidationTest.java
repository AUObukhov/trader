package ru.obukhov.trader.web.model;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.market.model.CandleResolution;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.trading.model.StrategyType;

class BotConfigValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final BotConfig botConfig = BotConfig.builder()
                .ticker("ticker")
                .candleResolution(CandleResolution._1MIN)
                .commission(0.003)
                .strategyType(StrategyType.CONSERVATIVE)
                .build();
        AssertUtils.assertNoViolations(botConfig);
    }

    @Test
    void validationFails_whenTickerIsNull() {
        final BotConfig botConfig = BotConfig.builder()
                .candleResolution(CandleResolution._1MIN)
                .commission(0.003)
                .strategyType(StrategyType.CONSERVATIVE)
                .build();

        AssertUtils.assertViolation(botConfig, "ticker is mandatory");
    }

    @Test
    void validationFails_whenCandleResolutionIsNull() {
        final BotConfig botConfig = BotConfig.builder()
                .ticker("ticker")
                .commission(0.003)
                .strategyType(StrategyType.CONSERVATIVE)
                .build();

        AssertUtils.assertViolation(botConfig, "candleResolution is mandatory");
    }

    @Test
    void validationFails_whenCommissionIsNull() {
        final BotConfig botConfig = BotConfig.builder()
                .ticker("ticker")
                .candleResolution(CandleResolution._1MIN)
                .strategyType(StrategyType.CONSERVATIVE)
                .build();

        AssertUtils.assertViolation(botConfig, "commission is mandatory");
    }

    @Test
    void validationFails_whenStrategyTypeIsNull() {
        final BotConfig botConfig = BotConfig.builder()
                .ticker("ticker")
                .candleResolution(CandleResolution._1MIN)
                .commission(0.003)
                .build();
        AssertUtils.assertViolation(botConfig, "strategyType is mandatory");
    }

}