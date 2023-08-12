package ru.obukhov.trader.web.model.exchange;

import org.junit.jupiter.api.Test;
import org.quartz.CronExpression;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

class BackTestRequestValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() throws ParseException {
        final BackTestRequest request = createValidBackTestRequest();

        AssertUtils.assertNoViolations(request);
    }

    @Test
    void validationFails_whenFromIsNull() throws ParseException {
        final BackTestRequest request = createValidBackTestRequest();
        request.setFrom(null);

        AssertUtils.assertViolation(request, "from is mandatory");
    }

    @Test
    void validationFails_whenBalanceConfigIsNull() throws ParseException {
        final BackTestRequest request = createValidBackTestRequest();
        request.setBalanceConfig(null);

        AssertUtils.assertViolation(request, "balanceConfig is mandatory");
    }

    // region botConfigs validation tests

    @Test
    void validationFails_whenBotsConfigsIsNull() throws ParseException {
        final BackTestRequest request = createValidBackTestRequest();
        request.setBotConfigs(null);

        AssertUtils.assertViolation(request, "botConfigs is mandatory");
    }

    @Test
    void validationFails_whenBotsConfigsIsEmpty() throws ParseException {
        final BackTestRequest request = createValidBackTestRequest();
        request.setBotConfigs(Collections.emptyList());

        AssertUtils.assertViolation(request, "botConfigs is mandatory");
    }

    @Test
    void validationFails_whenCandleAccountIdIsNull() throws ParseException {
        final BackTestRequest request = createValidBackTestRequest();
        final BotConfig botConfig = new BotConfig(
                null,
                TestShare1.FIGI,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.setDefaultScale(0.003),
                StrategyType.CONSERVATIVE,
                null
        );
        request.setBotConfigs(List.of(botConfig));

        AssertUtils.assertViolation(request, "accountId is mandatory");
    }

    @Test
    void validationFails_whenCandleIntervalIsNull() throws ParseException {
        final BackTestRequest request = createValidBackTestRequest();
        final BotConfig botConfig = new BotConfig(
                TestData.ACCOUNT_ID1,
                TestShare1.FIGI,
                null,
                DecimalUtils.setDefaultScale(0.003),
                StrategyType.CONSERVATIVE,
                null
        );
        request.setBotConfigs(List.of(botConfig));

        AssertUtils.assertViolation(request, "candleInterval is mandatory");
    }

    @Test
    void validationFails_whenCommissionIsNull() throws ParseException {
        final BackTestRequest request = createValidBackTestRequest();
        final BotConfig botConfig = new BotConfig(
                TestData.ACCOUNT_ID1,
                TestShare1.FIGI,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                null,
                StrategyType.CONSERVATIVE,
                null
        );
        request.setBotConfigs(List.of(botConfig));

        AssertUtils.assertViolation(request, "commission is mandatory");
    }

    @Test
    void validationFails_whenStrategyTypeIsNull() throws ParseException {
        final BackTestRequest request = createValidBackTestRequest();
        final BotConfig botConfig = new BotConfig(
                TestData.ACCOUNT_ID1,
                TestShare1.FIGI,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.setDefaultScale(0.003),
                null,
                null
        );
        request.setBotConfigs(List.of(botConfig));

        AssertUtils.assertViolation(request, "strategyType is mandatory");
    }

    // endregion

    private BackTestRequest createValidBackTestRequest() throws ParseException {
        final BackTestRequest request = new BackTestRequest();

        BalanceConfig balanceConfig = new BalanceConfig(DecimalUtils.setDefaultScale(10), DecimalUtils.setDefaultScale(1), new CronExpression("0 0 0 1 * ?"));
        request.setBalanceConfig(balanceConfig);

        request.setFrom(OffsetDateTime.now());

        final BotConfig botConfig = new BotConfig(
                TestData.ACCOUNT_ID1,
                TestShare1.FIGI,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.setDefaultScale(0.003),
                StrategyType.CONSERVATIVE,
                null
        );
        request.setBotConfigs(List.of(botConfig));

        return request;
    }

}