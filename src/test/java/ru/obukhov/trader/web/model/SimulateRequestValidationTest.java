package ru.obukhov.trader.web.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.quartz.CronExpression;
import ru.obukhov.trader.config.BotConfig;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.exchange.SimulateRequest;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

class SimulateRequestValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();

        AssertUtils.assertNoViolations(request);
    }

    // region ticker validations tests

    @Test
    void validationFails_whenTickerIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setTicker(null);

        AssertUtils.assertViolation(request, "ticker is mandatory");
    }

    @Test
    void validationFails_whenTickerIsEmpty() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setTicker(StringUtils.EMPTY);

        AssertUtils.assertViolation(request, "ticker is mandatory");
    }

    @Test
    void validationFails_whenTickerIsBlank() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setTicker("     ");

        AssertUtils.assertViolation(request, "ticker is mandatory");
    }

    // endregion

    @Test
    void validationFails_whenInitialBalanceIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setInitialBalance(null);

        AssertUtils.assertViolation(request, "initial balance is mandatory");
    }

    @Test
    void validationFails_whenBalanceIncrementIsNullAndBalanceIncrementCronIsNotNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setBalanceIncrement(null);

        AssertUtils.assertViolation(
                request,
                "balanceIncrement and balanceIncrementCron must be both null or not null"
        );
    }

    @Test
    void validationFails_whenBalanceIncrementIsNotNullAndBalanceIncrementCronIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setBalanceIncrementCron(null);

        AssertUtils.assertViolation(
                request,
                "balanceIncrement and balanceIncrementCron must be both null or not null"
        );
    }

    @Test
    void validationFails_whenFromIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setFrom(null);

        AssertUtils.assertViolation(request, "from is mandatory");
    }

    // region botsConfigs validation tests

    @Test
    void validationFails_whenBotsConfigsIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setBotsConfigs(null);

        AssertUtils.assertViolation(request, "botsConfigs is mandatory");
    }

    @Test
    void validationFails_whenBotsConfigsIsEmpty() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setBotsConfigs(Collections.emptyList());

        AssertUtils.assertViolation(request, "botsConfigs is mandatory");
    }

    @Test
    void validationFails_whenCandleResolutionIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.getBotsConfigs().get(0).setCandleResolution(null);

        AssertUtils.assertViolation(request, "candleResolution is mandatory");
    }

    @Test
    void validationFails_whenStrategyTypeIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.getBotsConfigs().get(0).setStrategyType(null);

        AssertUtils.assertViolation(request, "strategyType is mandatory");
    }

    // endregion

    private SimulateRequest createValidSimulationRequest() throws ParseException {
        final SimulateRequest request = new SimulateRequest();

        request.setTicker("ticker");

        request.setInitialBalance(BigDecimal.TEN);
        request.setBalanceIncrement(BigDecimal.ONE);
        request.setBalanceIncrementCron(new CronExpression("0 0 0 1 * ?"));

        request.setFrom(OffsetDateTime.now());

        final BotConfig botConfig = new BotConfig(CandleResolution._1MIN, StrategyType.CONSERVATIVE);
        request.setBotsConfigs(List.of(botConfig));

        return request;
    }

}