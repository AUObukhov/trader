package ru.obukhov.trader.web.model.pojo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.quartz.CronExpression;

import java.math.BigDecimal;
import java.text.ParseException;

class SimulationUnitUnitTest {

    @Test
    void isBalanceIncremented_returnsTrue_whenBalanceIncrementIsNull_andBalanceIncrementCronIsNotNull()
            throws ParseException {

        SimulationUnit unit = new SimulationUnit();
        unit.setBalanceIncrement(BigDecimal.TEN);
        unit.setBalanceIncrementCron(new CronExpression("0 0 0 1 * ?"));

        boolean result = unit.isBalanceIncremented();

        Assertions.assertTrue(result);
    }

    @Test
    void isBalanceIncremented_returnsFalse_whenBalanceIncrementIsNull_andBalanceIncrementCronIsNull() {

        SimulationUnit unit = new SimulationUnit();
        unit.setBalanceIncrement(null);
        unit.setBalanceIncrementCron(null);

        boolean result = unit.isBalanceIncremented();

        Assertions.assertFalse(result);
    }

    @Test
    void isBalanceIncremented_returnsFalse_whenBalanceIncrementIsNull_andBalanceIncrementCronIsNotNull()
            throws ParseException {

        SimulationUnit unit = new SimulationUnit();
        unit.setBalanceIncrement(null);
        unit.setBalanceIncrementCron(new CronExpression("0 0 0 1 * ?"));

        boolean result = unit.isBalanceIncremented();

        Assertions.assertFalse(result);
    }

    @Test
    void isBalanceIncremented_returnsFalse_whenBalanceIncrementIsNoyNull_andBalanceIncrementCronIsNull() {

        SimulationUnit unit = new SimulationUnit();
        unit.setBalanceIncrement(BigDecimal.TEN);
        unit.setBalanceIncrementCron(null);

        boolean result = unit.isBalanceIncremented();

        Assertions.assertFalse(result);
    }

}