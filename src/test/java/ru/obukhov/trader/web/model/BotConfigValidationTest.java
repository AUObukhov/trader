package ru.obukhov.trader.web.model;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

class BotConfigValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final BotConfig botConfig = BotConfig.builder()
                .accountId("2000124699")
                .ticker("ticker")
                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                .commission(0.003)
                .strategyType(StrategyType.CONSERVATIVE)
                .build();
        AssertUtils.assertNoViolations(botConfig);
    }

    @Test
    void validationFails_whenAccountIdIsNull() {
        final BotConfig botConfig = BotConfig.builder()
                .ticker("ticker")
                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                .commission(0.003)
                .strategyType(StrategyType.CONSERVATIVE)
                .build();
        AssertUtils.assertViolation(botConfig, "accountId is mandatory");
    }

    @Test
    void validationFails_whenTickerIsNull() {
        final BotConfig botConfig = BotConfig.builder()
                .accountId("2000124699")
                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                .commission(0.003)
                .strategyType(StrategyType.CONSERVATIVE)
                .build();

        AssertUtils.assertViolation(botConfig, "ticker is mandatory");
    }

    @Test
    void validationFails_whenCandleIntervalIsNull() {
        final BotConfig botConfig = BotConfig.builder()
                .accountId("2000124699")
                .ticker("ticker")
                .commission(0.003)
                .strategyType(StrategyType.CONSERVATIVE)
                .build();

        AssertUtils.assertViolation(botConfig, "candleInterval is mandatory");
    }

    @Test
    void validationFails_whenCommissionIsNull() {
        final BotConfig botConfig = BotConfig.builder()
                .accountId("2000124699")
                .ticker("ticker")
                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                .strategyType(StrategyType.CONSERVATIVE)
                .build();

        AssertUtils.assertViolation(botConfig, "commission is mandatory");
    }

    @Test
    void validationFails_whenStrategyTypeIsNull() {
        final BotConfig botConfig = BotConfig.builder()
                .accountId("2000124699")
                .ticker("ticker")
                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                .commission(0.003)
                .build();
        AssertUtils.assertViolation(botConfig, "strategyType is mandatory");
    }

}