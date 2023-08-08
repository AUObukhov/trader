package ru.obukhov.trader.market.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

class DataStructsHelperUnitTest {

    // region createMoneyValue tests

    @Test
    void createMoneyValue_withEnumCurrency() {
        final String currency = Currencies.EUR;
        final long units = 123;
        final int nano = 456;
        final BigDecimal value = DecimalUtils.createBigDecimal(units, nano);

        final MoneyValue moneyValue = DataStructsHelper.createMoneyValue(currency, value);

        Assertions.assertEquals(currency, moneyValue.getCurrency());
        Assertions.assertEquals(units, moneyValue.getUnits());
        Assertions.assertEquals(nano, moneyValue.getNano());
    }

    @Test
    void createMoneyValue_withStringCurrency() {
        final String currency = Currencies.EUR;
        final long units = 123;
        final int nano = 456;
        final BigDecimal value = DecimalUtils.createBigDecimal(units, nano);

        final MoneyValue moneyValue = DataStructsHelper.createMoneyValue(currency, value);

        Assertions.assertEquals(currency, moneyValue.getCurrency());
        Assertions.assertEquals(units, moneyValue.getUnits());
        Assertions.assertEquals(nano, moneyValue.getNano());
    }

    // endregion

    // region createMoney tests

    @Test
    void createMoney() {
        final String currency = Currencies.EUR;
        final BigDecimal value = DecimalUtils.setDefaultScale(123.456);

        final Money money = DataStructsHelper.createMoney(currency, value);

        Assertions.assertEquals(currency, money.getCurrency());
        AssertUtils.assertEquals(value, money.getValue());
    }

    // endregion

    // region createWithdrawLimits tests

    @Test
    void createWithdrawLimits_withMoneys() {
        // arrange

        final String currency1 = Currencies.EUR;
        final String currency2 = Currencies.USD;

        final BigDecimal value1 = DecimalUtils.setDefaultScale(123.456);
        final BigDecimal value2 = DecimalUtils.setDefaultScale(789.012);

        final MoneyValue moneyValue1 = DataStructsHelper.createMoneyValue(currency1, value1);
        final MoneyValue moneyValue2 = DataStructsHelper.createMoneyValue(currency2, value2);
        final List<MoneyValue> moneys = List.of(moneyValue1, moneyValue2);

        // action

        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys);

        // assert

        final Money expectedMoney1 = DataStructsHelper.createMoney(currency1, value1);
        final Money expectedMoney2 = DataStructsHelper.createMoney(currency2, value2);
        final List<Money> expectedMoneys = List.of(expectedMoney1, expectedMoney2);

        final Money expectedBlocked1 = DataStructsHelper.createMoney(currency1, DecimalUtils.setDefaultScale(0));
        final Money expectedBlocked2 = DataStructsHelper.createMoney(currency2, DecimalUtils.setDefaultScale(0));
        final List<Money> expectedBlocked = List.of(expectedBlocked1, expectedBlocked2);

        final Money expectedBlockedGuarantee1 = DataStructsHelper.createMoney(currency1, DecimalUtils.setDefaultScale(0));
        final Money expectedBlockedGuarantee2 = DataStructsHelper.createMoney(currency2, DecimalUtils.setDefaultScale(0));
        final List<Money> expectedBlockedGuarantee = List.of(expectedBlockedGuarantee1, expectedBlockedGuarantee2);

        Assertions.assertEquals(expectedMoneys, withdrawLimits.getMoney());
        Assertions.assertEquals(expectedBlocked, withdrawLimits.getBlocked());
        Assertions.assertEquals(expectedBlockedGuarantee, withdrawLimits.getBlockedGuarantee());
    }

    @Test
    void createWithdrawLimits_withMoneysAndBlocked() {
        // arrange

        final String currency1 = Currencies.EUR;
        final String currency2 = Currencies.USD;

        final BigDecimal value1 = DecimalUtils.setDefaultScale(123.456);
        final BigDecimal value2 = DecimalUtils.setDefaultScale(789.012);

        final MoneyValue moneyValue1 = DataStructsHelper.createMoneyValue(currency1, value1);
        final MoneyValue moneyValue2 = DataStructsHelper.createMoneyValue(currency2, value2);
        final List<MoneyValue> moneys = List.of(moneyValue1, moneyValue2);

        final BigDecimal blockedValue1 = DecimalUtils.setDefaultScale(12.34);
        final BigDecimal blockedValue2 = DecimalUtils.setDefaultScale(56.78);

        final MoneyValue blocked1 = DataStructsHelper.createMoneyValue(currency1, blockedValue1);
        final MoneyValue blocked2 = DataStructsHelper.createMoneyValue(currency2, blockedValue2);
        final List<MoneyValue> blocked = List.of(blocked1, blocked2);

        // action

        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys, blocked);

        // assert

        final Money expectedMoney1 = DataStructsHelper.createMoney(currency1, value1);
        final Money expectedMoney2 = DataStructsHelper.createMoney(currency2, value2);
        final List<Money> expectedMoneys = List.of(expectedMoney1, expectedMoney2);

        final Money expectedBlocked1 = DataStructsHelper.createMoney(currency1, blockedValue1);
        final Money expectedBlocked2 = DataStructsHelper.createMoney(currency2, blockedValue2);
        final List<Money> expectedBlocked = List.of(expectedBlocked1, expectedBlocked2);

        final Money expectedBlockedGuarantee1 = DataStructsHelper.createMoney(currency1, DecimalUtils.setDefaultScale(0));
        final Money expectedBlockedGuarantee2 = DataStructsHelper.createMoney(currency2, DecimalUtils.setDefaultScale(0));
        final List<Money> expectedBlockedGuarantee = List.of(expectedBlockedGuarantee1, expectedBlockedGuarantee2);

        Assertions.assertEquals(expectedMoneys, withdrawLimits.getMoney());
        Assertions.assertEquals(expectedBlocked, withdrawLimits.getBlocked());
        Assertions.assertEquals(expectedBlockedGuarantee, withdrawLimits.getBlockedGuarantee());
    }

    @Test
    void createWithdrawLimits_withMoneysAndBlockedAndBlockedGuarantee() {
        // arrange

        final String currency1 = Currencies.EUR;
        final String currency2 = Currencies.USD;

        final BigDecimal value1 = DecimalUtils.setDefaultScale(123.456);
        final BigDecimal value2 = DecimalUtils.setDefaultScale(789.012);

        final MoneyValue moneyValue1 = DataStructsHelper.createMoneyValue(currency1, value1);
        final MoneyValue moneyValue2 = DataStructsHelper.createMoneyValue(currency2, value2);
        final List<MoneyValue> moneys = List.of(moneyValue1, moneyValue2);

        final BigDecimal blockedValue1 = DecimalUtils.setDefaultScale(12.34);
        final BigDecimal blockedValue2 = DecimalUtils.setDefaultScale(56.78);

        final MoneyValue blocked1 = DataStructsHelper.createMoneyValue(currency1, blockedValue1);
        final MoneyValue blocked2 = DataStructsHelper.createMoneyValue(currency2, blockedValue2);
        final List<MoneyValue> blocked = List.of(blocked1, blocked2);

        final BigDecimal blockedGuaranteeValue1 = DecimalUtils.setDefaultScale(1.2);
        final BigDecimal blockedGuaranteeValue2 = DecimalUtils.setDefaultScale(3.4);

        final MoneyValue blockedGuarantee1 = DataStructsHelper.createMoneyValue(currency1, blockedGuaranteeValue1);
        final MoneyValue blockedGuarantee2 = DataStructsHelper.createMoneyValue(currency2, blockedGuaranteeValue2);
        final List<MoneyValue> blockedGuarantee = List.of(blockedGuarantee1, blockedGuarantee2);

        // action

        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys, blocked, blockedGuarantee);

        // assert

        final Money expectedMoney1 = DataStructsHelper.createMoney(currency1, value1);
        final Money expectedMoney2 = DataStructsHelper.createMoney(currency2, value2);
        final List<Money> expectedMoneys = List.of(expectedMoney1, expectedMoney2);

        final Money expectedBlocked1 = DataStructsHelper.createMoney(currency1, blockedValue1);
        final Money expectedBlocked2 = DataStructsHelper.createMoney(currency2, blockedValue2);
        final List<Money> expectedBlocked = List.of(expectedBlocked1, expectedBlocked2);

        final Money expectedBlockedGuarantee1 = DataStructsHelper.createMoney(currency1, blockedGuaranteeValue1);
        final Money expectedBlockedGuarantee2 = DataStructsHelper.createMoney(currency2, blockedGuaranteeValue2);
        final List<Money> expectedBlockedGuarantee = List.of(expectedBlockedGuarantee1, expectedBlockedGuarantee2);

        Assertions.assertEquals(expectedMoneys, withdrawLimits.getMoney());
        Assertions.assertEquals(expectedBlocked, withdrawLimits.getBlocked());
        Assertions.assertEquals(expectedBlockedGuarantee, withdrawLimits.getBlockedGuarantee());
    }

    // endregion

    // region getBalance tests

    @Test
    void getBalance() {
        final String currency1 = Currencies.EUR;
        final BigDecimal value1 = DecimalUtils.setDefaultScale(123.456);
        final Money money1 = DataStructsHelper.createMoney(currency1, value1);

        final String currency2 = Currencies.USD;
        final BigDecimal value2 = DecimalUtils.setDefaultScale(78.9);
        final Money money2 = DataStructsHelper.createMoney(currency2, value2);

        final List<Money> moneys = List.of(money1, money2);

        final BigDecimal balance1 = DataStructsHelper.getBalance(moneys, currency1);
        final BigDecimal balance2 = DataStructsHelper.getBalance(moneys, currency2);

        AssertUtils.assertEquals(value1, balance1);
        AssertUtils.assertEquals(value2, balance2);
    }

    @Test
    void getBalance_throwsNoSuchElementException_whenNoCurrency() {
        final String currency1 = Currencies.EUR;
        final BigDecimal value1 = DecimalUtils.setDefaultScale(123.456);
        final Money money1 = DataStructsHelper.createMoney(currency1, value1);

        final String currency2 = Currencies.USD;
        final BigDecimal value2 = DecimalUtils.setDefaultScale(78.9);
        final Money money2 = DataStructsHelper.createMoney(currency2, value2);

        final List<Money> moneys = List.of(money1, money2);

        Assertions.assertThrows(NoSuchElementException.class, () -> DataStructsHelper.getBalance(moneys, Currencies.RUB));
    }

    // endregion

}