package ru.obukhov.trader.web.model.exchange;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.quartz.CronExpression;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.TradingConfig;
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
    void validationFails_whenFromIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setFrom(null);

        AssertUtils.assertViolation(request, "from is mandatory");
    }

    @Test
    void validationFails_whenBalanceConfigIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setBalanceConfig(null);

        AssertUtils.assertViolation(request, "balanceConfig is mandatory");
    }

    // region tradingConfigs validation tests

    @Test
    void validationFails_whenBotsConfigsIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setTradingConfigs(null);

        AssertUtils.assertViolation(request, "tradingConfigs is mandatory");
    }

    @Test
    void validationFails_whenBotsConfigsIsEmpty() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setTradingConfigs(Collections.emptyList());

        AssertUtils.assertViolation(request, "tradingConfigs is mandatory");
    }

    @Test
    void validationFails_whenCandleResolutionIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.getTradingConfigs().get(0).setCandleResolution(null);

        AssertUtils.assertViolation(request, "candleResolution is mandatory");
    }

    @Test
    void validationFails_whenStrategyTypeIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.getTradingConfigs().get(0).setStrategyType(null);

        AssertUtils.assertViolation(request, "strategyType is mandatory");
    }

    // endregion

    private SimulateRequest createValidSimulationRequest() throws ParseException {
        final SimulateRequest request = new SimulateRequest();

        request.setTicker("ticker");

        BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?"));
        request.setBalanceConfig(balanceConfig);

        request.setFrom(OffsetDateTime.now());

        final TradingConfig tradingConfig = new TradingConfig(CandleResolution._1MIN, StrategyType.CONSERVATIVE);
        request.setTradingConfigs(List.of(tradingConfig));

        return request;
    }

}