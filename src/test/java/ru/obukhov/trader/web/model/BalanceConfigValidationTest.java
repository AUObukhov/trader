package ru.obukhov.trader.web.model;

import org.junit.jupiter.api.Test;
import org.quartz.CronExpression;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Map;

class BalanceConfigValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final BalanceConfig balanceConfig = TestData.newBalanceConfig(Currencies.RUB, 10.0, 1.0, "0 0 0 1 * ?");

        AssertUtils.assertNoViolations(balanceConfig);
    }

    @Test
    void validationFails_whenInitialBalancesIsNull() {
        final BalanceConfig balanceConfig = new BalanceConfig();
        balanceConfig.setInitialBalances(null);

        AssertUtils.assertViolation(balanceConfig, "initial balances are mandatory");
    }

    @Test
    void validationFails_whenInitialBalancesIsEmpty() {
        final BalanceConfig balanceConfig = new BalanceConfig(Map.of(), null, null);

        AssertUtils.assertViolation(balanceConfig, "initial balances are mandatory");
    }

    @Test
    void validationFails_whenBalanceIncrementIsNullAndBalanceIncrementCronIsNotNull() throws ParseException {
        final Map<String, BigDecimal> initialBalances = TestData.newDecimalMap(Currencies.RUB, 1000);
        final BalanceConfig balanceConfig = new BalanceConfig(initialBalances, null, new CronExpression("0 0 0 1 * ?"));

        AssertUtils.assertViolation(balanceConfig, "balanceIncrements and balanceIncrementCron must be both null or not null");
    }

    @Test
    void validationFails_whenBalanceIncrementIsNotNullAndBalanceIncrementCronIsNull() {
        final BalanceConfig balanceConfig = TestData.newBalanceConfig(Currencies.RUB, 10.0, 1.0);

        AssertUtils.assertViolation(balanceConfig, "balanceIncrements and balanceIncrementCron must be both null or not null");
    }

}