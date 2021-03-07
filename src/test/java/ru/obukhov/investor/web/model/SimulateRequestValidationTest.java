package ru.obukhov.investor.web.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
    void validationFails_whenTickerIsNull() {
        SimulateRequest request = createValidSimulationRequest();
        request.setTicker(null);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("ticker is mandatory", violations.iterator().next().getMessage());
    }

    @Test
    void validationFails_whenTickerIsEmpty() {
        SimulateRequest request = createValidSimulationRequest();
        request.setTicker(StringUtils.EMPTY);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("ticker is mandatory", violations.iterator().next().getMessage());
    }

    @Test
    void validationFails_whenTickerIsBlank() {
        SimulateRequest request = createValidSimulationRequest();
        request.setTicker("     ");

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("ticker is mandatory", violations.iterator().next().getMessage());
    }

    @Test
    void validationFails_whenBalanceIsNull() {
        SimulateRequest request = createValidSimulationRequest();
        request.setBalance(null);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("balance is mandatory", violations.iterator().next().getMessage());
    }

    @Test
    void validationFails_whenFromIsNull() {
        SimulateRequest request = createValidSimulationRequest();
        request.setFrom(null);

        Set<ConstraintViolation<SimulateRequest>> violations = validator.validate(request);
        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("from is mandatory", violations.iterator().next().getMessage());
    }

    private SimulateRequest createValidSimulationRequest() {
        SimulateRequest request = new SimulateRequest();
        request.setTicker("ticker");
        request.setBalance(BigDecimal.TEN);
        request.setFrom(OffsetDateTime.now());

        return request;
    }

}