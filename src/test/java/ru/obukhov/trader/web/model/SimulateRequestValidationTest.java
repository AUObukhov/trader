package ru.obukhov.trader.web.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.CronExpression;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.web.model.exchange.SimulateRequest;
import ru.obukhov.trader.web.model.pojo.SimulationUnit;

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
    void validationFails_whenSimulationsUnitsAreNull() throws ParseException {
        SimulateRequest request = createValidSimulationRequest();
        request.setSimulationUnits(null);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "simulationUnits are mandatory");
    }

    @Test
    void validationFails_whenSimulationsUnitsAreEmpty() throws ParseException {
        SimulateRequest request = createValidSimulationRequest();
        request.setSimulationUnits(Collections.emptyList());

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "simulationUnits are mandatory");
    }

    @Test
    void validationFails_whenTickerIsNull() throws ParseException {
        SimulateRequest request = createValidSimulationRequest();
        request.getSimulationUnits().get(0).setTicker(null);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "ticker in simulation unit is mandatory");
    }

    @Test
    void validationFails_whenTickerIsEmpty() throws ParseException {
        SimulateRequest request = createValidSimulationRequest();
        request.getSimulationUnits().get(0).setTicker(StringUtils.EMPTY);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "ticker in simulation unit is mandatory");
    }

    @Test
    void validationFails_whenTickerIsBlank() throws ParseException {
        SimulateRequest request = createValidSimulationRequest();
        request.getSimulationUnits().get(0).setTicker("     ");

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "ticker in simulation unit is mandatory");
    }

    @Test
    void validationFails_whenInitialBalanceIsNull() throws ParseException {
        SimulateRequest request = createValidSimulationRequest();
        request.getSimulationUnits().get(0).setInitialBalance(null);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "initial balance in simulation unit is mandatory");
    }

    @Test
    void validationFails_whenBalanceIncrementIsNullAndBalanceIncrementCronIsNotNull() throws ParseException {
        SimulateRequest request = createValidSimulationRequest();
        request.getSimulationUnits().get(0).setBalanceIncrement(null);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations,
                "balanceIncrement and balanceIncrementCron must be both null or not null");
    }

    @Test
    void validationFails_whenBalanceIncrementIsNotNullAndBalanceIncrementCronIsNull() throws ParseException {
        SimulateRequest request = createValidSimulationRequest();
        request.getSimulationUnits().get(0).setBalanceIncrementCron(null);

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

    @Test
    void validationFails_whenSimulationUnitsContainNotUniqueTickers() throws ParseException {
        SimulateRequest request = createValidSimulationRequest();

        SimulationUnit simulationUnit1 = new SimulationUnit();
        simulationUnit1.setTicker("ticker");
        simulationUnit1.setInitialBalance(BigDecimal.valueOf(100));
        simulationUnit1.setBalanceIncrement(BigDecimal.valueOf(10));
        simulationUnit1.setBalanceIncrementCron(new CronExpression("0 0 0 1 * ?"));

        SimulationUnit simulationUnit2 = new SimulationUnit();
        simulationUnit2.setTicker("ticker");
        simulationUnit2.setInitialBalance(BigDecimal.valueOf(200));
        simulationUnit2.setBalanceIncrement(BigDecimal.valueOf(20));
        simulationUnit2.setBalanceIncrementCron(new CronExpression("0 0 0 2 * ?"));

        request.setSimulationUnits(List.of(simulationUnit1, simulationUnit2));

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations,
                "simulation units in collection must contain different tickers");
    }

    private SimulateRequest createValidSimulationRequest() throws ParseException {
        SimulateRequest request = new SimulateRequest();

        SimulationUnit simulationUnit = new SimulationUnit();
        simulationUnit.setTicker("ticker");
        simulationUnit.setInitialBalance(BigDecimal.TEN);
        simulationUnit.setBalanceIncrement(BigDecimal.ONE);
        simulationUnit.setBalanceIncrementCron(new CronExpression("0 0 0 1 * ?"));
        request.setSimulationUnits(List.of(simulationUnit));

        request.setFrom(OffsetDateTime.now());

        return request;
    }

}