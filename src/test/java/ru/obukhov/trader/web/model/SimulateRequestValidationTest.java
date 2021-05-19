package ru.obukhov.trader.web.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.CronExpression;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.web.model.exchange.SimulateRequest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.Set;

class SimulateRequestValidationTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validationSucceeds_whenEverythingIsValid() throws ParseException {
        SimulateRequest request = createValidSimulationRequest();

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        Assertions.assertTrue(violations.isEmpty());
    }

    @Test
    void validationFails_whenTickerIsNull() throws ParseException {
        SimulateRequest request = createValidSimulationRequest();
        request.setTicker(null);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "ticker is mandatory");
    }

    @Test
    void validationFails_whenTickerIsEmpty() throws ParseException {
        SimulateRequest request = createValidSimulationRequest();
        request.setTicker(StringUtils.EMPTY);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "ticker is mandatory");
    }

    @Test
    void validationFails_whenTickerIsBlank() throws ParseException {
        SimulateRequest request = createValidSimulationRequest();
        request.setTicker("     ");

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "ticker is mandatory");
    }

    @Test
    void validationFails_whenInitialBalanceIsNull() throws ParseException {
        SimulateRequest request = createValidSimulationRequest();
        request.setInitialBalance(null);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "initial balance is mandatory");
    }

    @Test
    void validationFails_whenBalanceIncrementIsNullAndBalanceIncrementCronIsNotNull() throws ParseException {
        SimulateRequest request = createValidSimulationRequest();
        request.setBalanceIncrement(null);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations,
                "balanceIncrement and balanceIncrementCron must be both null or not null");
    }

    @Test
    void validationFails_whenBalanceIncrementIsNotNullAndBalanceIncrementCronIsNull() throws ParseException {
        SimulateRequest request = createValidSimulationRequest();
        request.setBalanceIncrementCron(null);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations,
                "balanceIncrement and balanceIncrementCron must be both null or not null");
    }

    @Test
    void validationFails_whenFromIsNull() throws ParseException {
        SimulateRequest request = createValidSimulationRequest();
        request.setFrom(null);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "from is mandatory");
    }

    private SimulateRequest createValidSimulationRequest() throws ParseException {
        SimulateRequest request = new SimulateRequest();

        request.setTicker("ticker");
        request.setInitialBalance(BigDecimal.TEN);
        request.setBalanceIncrement(BigDecimal.ONE);
        request.setBalanceIncrementCron(new CronExpression("0 0 0 1 * ?"));

        request.setFrom(OffsetDateTime.now());

        return request;
    }

}