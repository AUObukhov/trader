package ru.obukhov.trader.web.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.quartz.CronExpression;
import ru.obukhov.trader.bot.model.StrategyConfig;
import ru.obukhov.trader.bot.model.StrategyType;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.web.model.exchange.SimulateRequest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class SimulateRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validationSucceeds_whenEverythingIsValid() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();

        final Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        Assertions.assertTrue(violations.isEmpty());
    }

    // region ticker validations tests

    @Test
    void validationFails_whenTickerIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setTicker(null);

        final Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "ticker is mandatory");
    }

    @Test
    void validationFails_whenTickerIsEmpty() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setTicker(StringUtils.EMPTY);

        final Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "ticker is mandatory");
    }

    @Test
    void validationFails_whenTickerIsBlank() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setTicker("     ");

        final Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "ticker is mandatory");
    }

    // endregion

    @Test
    void validationFails_whenInitialBalanceIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setInitialBalance(null);

        final Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "initial balance is mandatory");
    }

    @Test
    void validationFails_whenBalanceIncrementIsNullAndBalanceIncrementCronIsNotNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setBalanceIncrement(null);

        final Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations,
                "balanceIncrement and balanceIncrementCron must be both null or not null");
    }

    @Test
    void validationFails_whenBalanceIncrementIsNotNullAndBalanceIncrementCronIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setBalanceIncrementCron(null);

        final Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations,
                "balanceIncrement and balanceIncrementCron must be both null or not null");
    }

    @Test
    void validationFails_whenFromIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setFrom(null);

        final Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "from is mandatory");
    }

    // region strategiesConfigs validation tests

    @Test
    void validationFails_whenStrategiesConfigsIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setStrategiesConfigs(null);

        final Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "strategiesConfigs is mandatory");
    }

    @Test
    void validationFails_whenStrategiesConfigsIsEmpty() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.setStrategiesConfigs(Collections.emptyList());

        final Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "strategiesConfigs is mandatory");
    }

    @Test
    void validationFails_whenStrategyTypeIsNull() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.getStrategiesConfigs().get(0).setType(null);

        final Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "type in StrategyConfig is mandatory");
    }

    @Test
    void validationFails_whenMinimumProfitIsBelowZero() throws ParseException {
        final SimulateRequest request = createValidSimulationRequest();
        request.getStrategiesConfigs().get(0).setMinimumProfit(-0.1f);

        final Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "должно быть не меньше 0");
    }

    // endregion

    private SimulateRequest createValidSimulationRequest() throws ParseException {
        final SimulateRequest request = new SimulateRequest();

        request.setTicker("ticker");

        request.setInitialBalance(BigDecimal.TEN);
        request.setBalanceIncrement(BigDecimal.ONE);
        request.setBalanceIncrementCron(new CronExpression("0 0 0 1 * ?"));

        request.setFrom(OffsetDateTime.now());

        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.CONSERVATIVE, 0.1f);
        request.setStrategiesConfigs(List.of(strategyConfig));

        return request;
    }

}