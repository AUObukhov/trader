package ru.obukhov.trader.web.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.support.CronExpression;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.test.utils.model.TestData;

import java.math.BigDecimal;
import java.util.Map;

class BalanceConfigUnitTest {

    @Test
    void constructor_setsValues() {
        final Map<String, BigDecimal> initialBalances = TestData.newDecimalMap(Currencies.RUB, 10000, Currencies.USD, 1000);
        final Map<String, BigDecimal> balanceIncrements = TestData.newDecimalMap(Currencies.RUB, 5000, Currencies.USD, 500);
        final CronExpression balanceIncrementCron = CronExpression.parse("0 0 * * * ?");

        final BalanceConfig balanceConfig = new BalanceConfig(initialBalances, balanceIncrements, balanceIncrementCron.toString());

        Assertions.assertEquals(initialBalances, balanceConfig.getInitialBalances());
        Assertions.assertEquals(balanceIncrements, balanceConfig.getBalanceIncrements());
        Assertions.assertEquals(balanceIncrementCron, balanceConfig.getBalanceIncrementCron());
    }

    @Test
    void constructor_clonesMaps() {
        final Map<String, BigDecimal> initialBalances = TestData.newDecimalMap(Currencies.RUB, 10000);
        final Map<String, BigDecimal> balanceIncrements = TestData.newDecimalMap(Currencies.RUB, 5000);

        final BalanceConfig balanceConfig = new BalanceConfig(initialBalances, balanceIncrements, "0 0 * * * ?");

        Assertions.assertNotSame(initialBalances, balanceConfig.getInitialBalances());
        Assertions.assertNotSame(balanceIncrements, balanceConfig.getBalanceIncrements());
    }

    @Test
    void constructor_acceptsNullBalanceIncrementsAndBalanceIncrementsCron() {
        final Map<String, BigDecimal> initialBalances = TestData.newDecimalMap(Currencies.RUB, 10000);

        final BalanceConfig balanceConfig = new BalanceConfig(initialBalances, null, null);

        Assertions.assertNull(balanceConfig.getBalanceIncrements());
        Assertions.assertNull(balanceConfig.getBalanceIncrementCron());
    }

}
