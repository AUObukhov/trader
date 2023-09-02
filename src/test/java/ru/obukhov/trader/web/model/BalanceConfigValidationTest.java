package ru.obukhov.trader.web.model;

import org.junit.jupiter.api.Test;
import org.quartz.CronExpression;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.text.ParseException;

class BalanceConfigValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() throws ParseException {
        final BalanceConfig balanceConfig = new BalanceConfig(
                DecimalUtils.setDefaultScale(10),
                DecimalUtils.setDefaultScale(1),
                new CronExpression("0 0 0 1 * ?")
        );

        AssertUtils.assertNoViolations(balanceConfig);
    }

    @Test
    void validationFails_whenInitialBalanceIsNull() throws ParseException {
        final BalanceConfig balanceConfig = new BalanceConfig(
                null,
                DecimalUtils.setDefaultScale(1),
                new CronExpression("0 0 0 1 * ?")
        );

        AssertUtils.assertViolation(balanceConfig, "initial balance is mandatory");
    }

    @Test
    void validationFails_whenBalanceIncrementIsNegative() throws ParseException {
        final BalanceConfig balanceConfig = new BalanceConfig(
                DecimalUtils.setDefaultScale(10),
                DecimalUtils.setDefaultScale(-1),
                new CronExpression("0 0 0 1 * ?")
        );

        AssertUtils.assertViolation(balanceConfig, "balanceIncrement must be positive");
    }

    @Test
    void validationFails_whenBalanceIncrementIsZero() throws ParseException {
        final BalanceConfig balanceConfig = new BalanceConfig(
                DecimalUtils.setDefaultScale(10),
                DecimalUtils.setDefaultScale(0),
                new CronExpression("0 0 0 1 * ?")
        );

        AssertUtils.assertViolation(balanceConfig, "balanceIncrement must be positive");
    }

    @Test
    void validationFails_whenBalanceIncrementIsNullAndBalanceIncrementCronIsNotNull() throws ParseException {
        final BalanceConfig balanceConfig = new BalanceConfig(
                DecimalUtils.setDefaultScale(10),
                null, new CronExpression("0 0 0 1 * ?")
        );

        AssertUtils.assertViolation(balanceConfig, "balanceIncrement and balanceIncrementCron must be both null or not null");
    }

    @Test
    void validationFails_whenBalanceIncrementIsNotNullAndBalanceIncrementCronIsNull() {
        final BalanceConfig balanceConfig = new BalanceConfig(
                DecimalUtils.setDefaultScale(10),
                DecimalUtils.setDefaultScale(1),
                null
        );

        AssertUtils.assertViolation(balanceConfig, "balanceIncrement and balanceIncrementCron must be both null or not null");
    }

}