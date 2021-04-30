package ru.obukhov.trader.web.model.validation.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.quartz.CronExpression;
import ru.obukhov.trader.web.model.pojo.SimulationUnit;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class SimulationUnitsAreDistinctValidatorUnitTest {

    @Test
    void isValid_returnsTrue_whenSimulationUnitsIsNull() {
        SimulationUnitsAreDistinctValidator validator = new SimulationUnitsAreDistinctValidator();

        boolean result = validator.isValid(null, null);

        Assertions.assertTrue(result);
    }

    @Test
    void isValid_returnsTrue_whenSimulationUnitsIsEmpty() {
        SimulationUnitsAreDistinctValidator validator = new SimulationUnitsAreDistinctValidator();

        Collection<SimulationUnit> simulationUnits = Collections.emptyList();

        boolean result = validator.isValid(simulationUnits, null);

        Assertions.assertTrue(result);
    }

    @Test
    void isValid_returnsTrue_whenSingleSimulationUnit() throws ParseException {
        SimulationUnitsAreDistinctValidator validator = new SimulationUnitsAreDistinctValidator();

        SimulationUnit simulationUnit = new SimulationUnit();
        simulationUnit.setTicker("ticker1");
        simulationUnit.setInitialBalance(BigDecimal.valueOf(100));
        simulationUnit.setBalanceIncrement(BigDecimal.valueOf(10));
        simulationUnit.setBalanceIncrementCron(new CronExpression("0 0 0 1 * ?"));

        Collection<SimulationUnit> simulationUnits = List.of(simulationUnit);

        boolean result = validator.isValid(simulationUnits, null);

        Assertions.assertTrue(result);
    }

    @Test
    void isValid_returnsTrue_whenTickersAreUnique() throws ParseException {
        SimulationUnitsAreDistinctValidator validator = new SimulationUnitsAreDistinctValidator();

        SimulationUnit simulationUnit1 = new SimulationUnit();
        simulationUnit1.setTicker("ticker1");
        simulationUnit1.setInitialBalance(BigDecimal.valueOf(100));
        simulationUnit1.setBalanceIncrement(BigDecimal.valueOf(10));
        simulationUnit1.setBalanceIncrementCron(new CronExpression("0 0 0 1 * ?"));

        SimulationUnit simulationUnit2 = new SimulationUnit();
        simulationUnit2.setTicker("ticker2");
        simulationUnit2.setInitialBalance(BigDecimal.valueOf(200));
        simulationUnit2.setBalanceIncrement(BigDecimal.valueOf(20));
        simulationUnit2.setBalanceIncrementCron(new CronExpression("0 0 0 2 * ?"));
        Collection<SimulationUnit> simulationUnits = List.of(simulationUnit1, simulationUnit2);

        boolean result = validator.isValid(simulationUnits, null);

        Assertions.assertTrue(result);
    }

    @Test
    void isValid_returnsTrue_whenTickersAreEqual() throws ParseException {
        SimulationUnitsAreDistinctValidator validator = new SimulationUnitsAreDistinctValidator();

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
        Collection<SimulationUnit> simulationUnits = List.of(simulationUnit1, simulationUnit2);

        boolean result = validator.isValid(simulationUnits, null);

        Assertions.assertFalse(result);
    }

}