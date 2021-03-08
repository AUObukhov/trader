package ru.obukhov.investor.web.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.obukhov.investor.test.utils.AssertUtils;
import ru.obukhov.investor.web.model.exchange.SimulateRequest;
import ru.obukhov.investor.web.model.pojo.SimulationUnit;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;

class SimulateRequestValidationTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        SimulateRequest request = createValidSimulationRequest();

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        Assertions.assertTrue(violations.isEmpty());
    }

    @Test
    void validationFails_whenSimulationsUnitsAreNull() {
        SimulateRequest request = createValidSimulationRequest();
        request.setSimulationUnits(null);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "simulationUnits are mandatory");
    }

    @Test
    void validationFails_whenSimulationsUnitsAreEmpty() {
        SimulateRequest request = createValidSimulationRequest();
        request.setSimulationUnits(Collections.emptyList());

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "simulationUnits are mandatory");
    }

    @Test
    void validationFails_whenTickerIsNull() {
        SimulateRequest request = createValidSimulationRequest();
        request.getSimulationUnits().get(0).setTicker(null);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "ticker in simulation unit is mandatory");
    }

    @Test
    void validationFails_whenTickerIsEmpty() {
        SimulateRequest request = createValidSimulationRequest();
        request.getSimulationUnits().get(0).setTicker(StringUtils.EMPTY);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "ticker in simulation unit is mandatory");
    }

    @Test
    void validationFails_whenTickerIsBlank() {
        SimulateRequest request = createValidSimulationRequest();
        request.getSimulationUnits().get(0).setTicker("     ");

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "ticker in simulation unit is mandatory");
    }

    @Test
    void validationFails_whenBalanceIsNull() {
        SimulateRequest request = createValidSimulationRequest();
        request.getSimulationUnits().get(0).setBalance(null);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "balance in simulation unit is mandatory");
    }

    @Test
    void validationFails_whenFromIsNull() {
        SimulateRequest request = createValidSimulationRequest();
        request.setFrom(null);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        AssertUtils.assertViolation(violations, "from is mandatory");
    }

    private SimulateRequest createValidSimulationRequest() {
        SimulateRequest request = new SimulateRequest();

        SimulationUnit simulationUnit = new SimulationUnit();
        simulationUnit.setTicker("ticker");
        simulationUnit.setBalance(BigDecimal.TEN);
        request.setSimulationUnits(Collections.singletonList(simulationUnit));

        request.setFrom(OffsetDateTime.now());

        return request;
    }

}