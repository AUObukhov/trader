package ru.obukhov.trader.web.model.exchange;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

class BackTestRequestValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final BackTestRequest request = createValidBackTestRequest();

        AssertUtils.assertNoViolations(request);
    }

    @Test
    void validationFails_whenFromIsNull() {
        final BackTestRequest request = createValidBackTestRequest();
        request.setFrom(null);

        AssertUtils.assertViolation(request, "from is mandatory");
    }

    @Test
    void validationFails_whenBalanceConfigIsNull() {
        final BackTestRequest request = createValidBackTestRequest();
        request.setBalanceConfig(null);

        AssertUtils.assertViolation(request, "balanceConfig is mandatory");
    }

    // region botConfigs validation tests

    @Test
    void validationFails_whenBotsConfigsIsNull() {
        final BackTestRequest request = createValidBackTestRequest();
        request.setBotConfigs(null);

        AssertUtils.assertViolation(request, "botConfigs is mandatory");
    }

    @Test
    void validationFails_whenBotsConfigsIsEmpty() {
        final BackTestRequest request = createValidBackTestRequest();
        request.setBotConfigs(Collections.emptyList());

        AssertUtils.assertViolation(request, "botConfigs is mandatory");
    }

    @Test
    void validationFails_whenCandleAccountIdIsNull() {
        final BackTestRequest request = createValidBackTestRequest();
        final BotConfig botConfig = new BotConfig(
                null,
                TestShares.APPLE.share().figi(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.setDefaultScale(0.003),
                StrategyType.CONSERVATIVE,
                null
        );
        request.setBotConfigs(List.of(botConfig));

        AssertUtils.assertViolation(request, "accountId is mandatory");
    }

    @Test
    void validationFails_whenCandleIntervalIsNull() {
        final BackTestRequest request = createValidBackTestRequest();
        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                TestShares.APPLE.share().figi(),
                null,
                DecimalUtils.setDefaultScale(0.003),
                StrategyType.CONSERVATIVE,
                null
        );
        request.setBotConfigs(List.of(botConfig));

        AssertUtils.assertViolation(request, "candleInterval is mandatory");
    }

    @Test
    void validationFails_whenCommissionIsNull() {
        final BackTestRequest request = createValidBackTestRequest();
        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                TestShares.APPLE.share().figi(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                null,
                StrategyType.CONSERVATIVE,
                null
        );
        request.setBotConfigs(List.of(botConfig));

        AssertUtils.assertViolation(request, "commission is mandatory");
    }

    @Test
    void validationFails_whenStrategyTypeIsNull() {
        final BackTestRequest request = createValidBackTestRequest();
        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                TestShares.APPLE.share().figi(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.setDefaultScale(0.003),
                null,
                null
        );
        request.setBotConfigs(List.of(botConfig));

        AssertUtils.assertViolation(request, "strategyType is mandatory");
    }

    // endregion

    private BackTestRequest createValidBackTestRequest() {
        final BackTestRequest request = new BackTestRequest();

        BalanceConfig balanceConfig = TestData.newBalanceConfig(Currencies.RUB, 10.0, 1.0, "0 0 0 1 * ?");
        request.setBalanceConfig(balanceConfig);

        request.setFrom(OffsetDateTime.now());

        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                TestShares.APPLE.share().figi(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.setDefaultScale(0.003),
                StrategyType.CONSERVATIVE,
                null
        );
        request.setBotConfigs(List.of(botConfig));

        return request;
    }

}