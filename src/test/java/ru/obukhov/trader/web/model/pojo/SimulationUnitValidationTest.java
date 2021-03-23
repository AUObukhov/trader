package ru.obukhov.trader.web.model.pojo;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.util.Set;

class SimulationUnitValidationTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        SimulationUnit unit = new SimulationUnit();
        unit.setTicker("ticker");
        unit.setInitialBalance(BigDecimal.TEN);

        Set<ConstraintViolation<SimulationUnit>> violations = validator.validate(unit);
        Assertions.assertTrue(violations.isEmpty());
    }

    @Test
    void validationFails_whenTickerIsNull() {
        SimulationUnit unit = new SimulationUnit();
        unit.setTicker(null);
        unit.setInitialBalance(BigDecimal.TEN);

        Set<ConstraintViolation<SimulationUnit>> violations = validator.validate(unit);
        AssertUtils.assertViolation(violations, "ticker in simulation unit is mandatory");
    }

    @Test
    void validationFails_whenTickerIsEmpty() {
        SimulationUnit unit = new SimulationUnit();
        unit.setTicker(StringUtils.EMPTY);
        unit.setInitialBalance(BigDecimal.TEN);

        Set<ConstraintViolation<SimulationUnit>> violations = validator.validate(unit);
        AssertUtils.assertViolation(violations, "ticker in simulation unit is mandatory");
    }

    @Test
    void validationFails_whenTickerIsBlank() {
        SimulationUnit unit = new SimulationUnit();
        unit.setTicker("    \n");
        unit.setInitialBalance(BigDecimal.TEN);

        Set<ConstraintViolation<SimulationUnit>> violations = validator.validate(unit);
        AssertUtils.assertViolation(violations, "ticker in simulation unit is mandatory");
    }

    @Test
    void validationFails_whenInitialBalanceIsNull() {
        SimulationUnit unit = new SimulationUnit();
        unit.setTicker("ticker");
        unit.setInitialBalance(null);

        Set<ConstraintViolation<SimulationUnit>> violations = validator.validate(unit);
        AssertUtils.assertViolation(violations, "initial balance in simulation unit is mandatory");
    }

}