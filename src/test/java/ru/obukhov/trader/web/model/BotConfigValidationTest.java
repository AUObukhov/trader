package ru.obukhov.trader.web.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class BotConfigValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                List.of(TestShares.APPLE.share().figi()),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.setDefaultScale(0.003),
                StrategyType.CONSERVATIVE,
                Collections.emptyMap()
        );
        AssertUtils.assertNoViolations(botConfig);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forViolation() {
        return Stream.of(
                Arguments.of(
                        null,
                        List.of(TestShares.APPLE.share().figi(), TestShares.SBER.share().figi()),
                        CandleInterval.CANDLE_INTERVAL_1_MIN,
                        0.003,
                        StrategyType.CONSERVATIVE,
                        Collections.emptyMap(),
                        "accountId is mandatory"
                ),
                Arguments.of(
                        TestAccounts.TINKOFF.account().id(),
                        null,
                        CandleInterval.CANDLE_INTERVAL_1_MIN,
                        0.003,
                        StrategyType.CONSERVATIVE,
                        Collections.emptyMap(),
                        "figies are mandatory"
                ),
                Arguments.of(
                        TestAccounts.TINKOFF.account().id(),
                        Collections.emptyList(),
                        CandleInterval.CANDLE_INTERVAL_1_MIN,
                        0.003,
                        StrategyType.CONSERVATIVE,
                        Collections.emptyMap(),
                        "figies are mandatory"
                ),
                Arguments.of(
                        TestAccounts.TINKOFF.account().id(),
                        List.of(TestShares.APPLE.share().figi(), TestShares.SBER.share().figi()),
                        null,
                        0.003,
                        StrategyType.CONSERVATIVE,
                        Collections.emptyMap(),
                        "candleInterval is mandatory"
                ),
                Arguments.of(
                        TestAccounts.TINKOFF.account().id(),
                        List.of(TestShares.APPLE.share().figi(), TestShares.SBER.share().figi()),
                        CandleInterval.CANDLE_INTERVAL_1_MIN,
                        null,
                        StrategyType.CONSERVATIVE,
                        Collections.emptyMap(),
                        "commission is mandatory"
                ),
                Arguments.of(
                        TestAccounts.TINKOFF.account().id(),
                        List.of(TestShares.APPLE.share().figi(), TestShares.SBER.share().figi()),
                        CandleInterval.CANDLE_INTERVAL_1_MIN,
                        0.003,
                        null,
                        Collections.emptyMap(),
                        "strategyType is mandatory"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forViolation")
    void testViolation(
            final String accountId,
            final List<String> figies,
            final CandleInterval candleInterval,
            final Double commission,
            final StrategyType strategyType,
            final Map<String, Object> strategyParams,
            final String expectedViolation
    ) {
        final BotConfig botConfig = new BotConfig(
                accountId,
                figies,
                candleInterval,
                DecimalUtils.setDefaultScale(commission),
                strategyType,
                strategyParams
        );
        AssertUtils.assertViolation(botConfig, expectedViolation);
    }

}