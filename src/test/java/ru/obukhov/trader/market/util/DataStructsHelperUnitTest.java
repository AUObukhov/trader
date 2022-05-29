package ru.obukhov.trader.market.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

class DataStructsHelperUnitTest {

    // region createMoneyValue tests

    @Test
    void createMoneyValue_withEnumCurrency() {
        final Currency currency = Currency.EUR;
        final long units = 123;
        final int nano = 456;
        final BigDecimal value = DecimalUtils.createBigDecimal(units, nano);

        final MoneyValue moneyValue = DataStructsHelper.createMoneyValue(currency, value);

        Assertions.assertEquals(currency.name(), moneyValue.getCurrency());
        Assertions.assertEquals(units, moneyValue.getUnits());
        Assertions.assertEquals(nano, moneyValue.getNano());
    }

    @Test
    void createMoneyValue_withStringCurrency() {
        final String currency = Currency.EUR.name();
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
        final java.util.Currency currency = Currency.EUR.getJavaCurrency();
        final BigDecimal value = BigDecimal.valueOf(123.456);

        final Money money = DataStructsHelper.createMoney(currency, value);

        Assertions.assertEquals(currency, money.getCurrency());
        AssertUtils.assertEquals(value, money.getValue());
    }

    // endregion

    // region createWithdrawLimits tests

    @Test
    void createWithdrawLimits_withMoneys() {
        // arrange

        final Currency currency1 = Currency.EUR;
        final Currency currency2 = Currency.USD;

        final BigDecimal value1 = BigDecimal.valueOf(123.456);
        final BigDecimal value2 = BigDecimal.valueOf(789.012);

        final MoneyValue moneyValue1 = DataStructsHelper.createMoneyValue(currency1, value1);
        final MoneyValue moneyValue2 = DataStructsHelper.createMoneyValue(currency2, value2);
        final List<MoneyValue> moneys = List.of(moneyValue1, moneyValue2);

        // action

        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys);

        // assert

        final Money expectedMoney1 = DataStructsHelper.createMoney(currency1.getJavaCurrency(), value1);
        final Money expectedMoney2 = DataStructsHelper.createMoney(currency2.getJavaCurrency(), value2);
        final List<Money> expectedMoneys = List.of(expectedMoney1, expectedMoney2);

        final Money expectedBlocked1 = DataStructsHelper.createMoney(currency1.getJavaCurrency(), BigDecimal.ZERO);
        final Money expectedBlocked2 = DataStructsHelper.createMoney(currency2.getJavaCurrency(), BigDecimal.ZERO);
        final List<Money> expectedBlocked = List.of(expectedBlocked1, expectedBlocked2);

        final Money expectedBlockedGuarantee1 = DataStructsHelper.createMoney(currency1.getJavaCurrency(), BigDecimal.ZERO);
        final Money expectedBlockedGuarantee2 = DataStructsHelper.createMoney(currency2.getJavaCurrency(), BigDecimal.ZERO);
        final List<Money> expectedBlockedGuarantee = List.of(expectedBlockedGuarantee1, expectedBlockedGuarantee2);

        Assertions.assertEquals(expectedMoneys, withdrawLimits.getMoney());
        Assertions.assertEquals(expectedBlocked, withdrawLimits.getBlocked());
        Assertions.assertEquals(expectedBlockedGuarantee, withdrawLimits.getBlockedGuarantee());
    }

    @Test
    void createWithdrawLimits_withMoneysAndBlocked() {
        // arrange

        final Currency currency1 = Currency.EUR;
        final Currency currency2 = Currency.USD;

        final BigDecimal value1 = BigDecimal.valueOf(123.456);
        final BigDecimal value2 = BigDecimal.valueOf(789.012);

        final MoneyValue moneyValue1 = DataStructsHelper.createMoneyValue(currency1, value1);
        final MoneyValue moneyValue2 = DataStructsHelper.createMoneyValue(currency2, value2);
        final List<MoneyValue> moneys = List.of(moneyValue1, moneyValue2);

        final BigDecimal blockedValue1 = BigDecimal.valueOf(12.34);
        final BigDecimal blockedValue2 = BigDecimal.valueOf(56.78);

        final MoneyValue blocked1 = DataStructsHelper.createMoneyValue(currency1, blockedValue1);
        final MoneyValue blocked2 = DataStructsHelper.createMoneyValue(currency2, blockedValue2);
        final List<MoneyValue> blocked = List.of(blocked1, blocked2);

        // action

        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys, blocked);

        // assert

        final Money expectedMoney1 = DataStructsHelper.createMoney(currency1.getJavaCurrency(), value1);
        final Money expectedMoney2 = DataStructsHelper.createMoney(currency2.getJavaCurrency(), value2);
        final List<Money> expectedMoneys = List.of(expectedMoney1, expectedMoney2);

        final Money expectedBlocked1 = DataStructsHelper.createMoney(currency1.getJavaCurrency(), blockedValue1);
        final Money expectedBlocked2 = DataStructsHelper.createMoney(currency2.getJavaCurrency(), blockedValue2);
        final List<Money> expectedBlocked = List.of(expectedBlocked1, expectedBlocked2);

        final Money expectedBlockedGuarantee1 = DataStructsHelper.createMoney(currency1.getJavaCurrency(), BigDecimal.ZERO);
        final Money expectedBlockedGuarantee2 = DataStructsHelper.createMoney(currency2.getJavaCurrency(), BigDecimal.ZERO);
        final List<Money> expectedBlockedGuarantee = List.of(expectedBlockedGuarantee1, expectedBlockedGuarantee2);

        Assertions.assertEquals(expectedMoneys, withdrawLimits.getMoney());
        Assertions.assertEquals(expectedBlocked, withdrawLimits.getBlocked());
        Assertions.assertEquals(expectedBlockedGuarantee, withdrawLimits.getBlockedGuarantee());
    }

    @Test
    void createWithdrawLimits_withMoneysAndBlockedAndBlockedGuarantee() {
        // arrange

        final Currency currency1 = Currency.EUR;
        final Currency currency2 = Currency.USD;

        final BigDecimal value1 = BigDecimal.valueOf(123.456);
        final BigDecimal value2 = BigDecimal.valueOf(789.012);

        final MoneyValue moneyValue1 = DataStructsHelper.createMoneyValue(currency1, value1);
        final MoneyValue moneyValue2 = DataStructsHelper.createMoneyValue(currency2, value2);
        final List<MoneyValue> moneys = List.of(moneyValue1, moneyValue2);

        final BigDecimal blockedValue1 = BigDecimal.valueOf(12.34);
        final BigDecimal blockedValue2 = BigDecimal.valueOf(56.78);

        final MoneyValue blocked1 = DataStructsHelper.createMoneyValue(currency1, blockedValue1);
        final MoneyValue blocked2 = DataStructsHelper.createMoneyValue(currency2, blockedValue2);
        final List<MoneyValue> blocked = List.of(blocked1, blocked2);

        final BigDecimal blockedGuaranteeValue1 = BigDecimal.valueOf(1.2);
        final BigDecimal blockedGuaranteeValue2 = BigDecimal.valueOf(3.4);

        final MoneyValue blockedGuarantee1 = DataStructsHelper.createMoneyValue(currency1, blockedGuaranteeValue1);
        final MoneyValue blockedGuarantee2 = DataStructsHelper.createMoneyValue(currency2, blockedGuaranteeValue2);
        final List<MoneyValue> blockedGuarantee = List.of(blockedGuarantee1, blockedGuarantee2);

        // action

        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys, blocked, blockedGuarantee);

        // assert

        final Money expectedMoney1 = DataStructsHelper.createMoney(currency1.getJavaCurrency(), value1);
        final Money expectedMoney2 = DataStructsHelper.createMoney(currency2.getJavaCurrency(), value2);
        final List<Money> expectedMoneys = List.of(expectedMoney1, expectedMoney2);

        final Money expectedBlocked1 = DataStructsHelper.createMoney(currency1.getJavaCurrency(), blockedValue1);
        final Money expectedBlocked2 = DataStructsHelper.createMoney(currency2.getJavaCurrency(), blockedValue2);
        final List<Money> expectedBlocked = List.of(expectedBlocked1, expectedBlocked2);

        final Money expectedBlockedGuarantee1 = DataStructsHelper.createMoney(currency1.getJavaCurrency(), blockedGuaranteeValue1);
        final Money expectedBlockedGuarantee2 = DataStructsHelper.createMoney(currency2.getJavaCurrency(), blockedGuaranteeValue2);
        final List<Money> expectedBlockedGuarantee = List.of(expectedBlockedGuarantee1, expectedBlockedGuarantee2);

        Assertions.assertEquals(expectedMoneys, withdrawLimits.getMoney());
        Assertions.assertEquals(expectedBlocked, withdrawLimits.getBlocked());
        Assertions.assertEquals(expectedBlockedGuarantee, withdrawLimits.getBlockedGuarantee());
    }

    // endregion

    // region getBalance tests

    @Test
    void getBalance() {
        final Currency currency1 = Currency.EUR;
        final BigDecimal value1 = BigDecimal.valueOf(123.456);
        final Money money1 = DataStructsHelper.createMoney(currency1.getJavaCurrency(), value1);

        final Currency currency2 = Currency.USD;
        final BigDecimal value2 = BigDecimal.valueOf(78.9);
        final Money money2 = DataStructsHelper.createMoney(currency2.getJavaCurrency(), value2);

        final List<Money> moneys = List.of(money1, money2);

        final BigDecimal balance1 = DataStructsHelper.getBalance(moneys, currency1);
        final BigDecimal balance2 = DataStructsHelper.getBalance(moneys, currency2);

        AssertUtils.assertEquals(value1, balance1);
        AssertUtils.assertEquals(value2, balance2);
    }

    @Test
    void getBalance_throwsNoSuchElementException_whenNoCurrency() {
        final Currency currency1 = Currency.EUR;
        final BigDecimal value1 = BigDecimal.valueOf(123.456);
        final Money money1 = DataStructsHelper.createMoney(currency1.getJavaCurrency(), value1);

        final Currency currency2 = Currency.USD;
        final BigDecimal value2 = BigDecimal.valueOf(78.9);
        final Money money2 = DataStructsHelper.createMoney(currency2.getJavaCurrency(), value2);

        final List<Money> moneys = List.of(money1, money2);

        Assertions.assertThrows(NoSuchElementException.class, () -> DataStructsHelper.getBalance(moneys, Currency.RUB));
    }

    // endregion

    // region createPostOrderResponse tests

    @Test
    void createPostOrderResponse_whenOrderIdIsNull() {
        final Currency currency = Currency.EUR;
        final double totalPrice = 1000;
        final double totalCommissionAmount = 5;
        final double currentPrice = 100;
        final long quantityLots = 10;
        final String figi = "figi";
        final OrderDirection direction = OrderDirection.ORDER_DIRECTION_BUY;
        final OrderType type = OrderType.ORDER_TYPE_MARKET;

        final PostOrderResponse response = DataStructsHelper.createPostOrderResponse(
                currency.name(),
                BigDecimal.valueOf(totalPrice),
                BigDecimal.valueOf(totalCommissionAmount),
                BigDecimal.valueOf(currentPrice),
                quantityLots,
                figi,
                direction,
                type,
                null
        );

        Assertions.assertEquals(OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_FILL, response.getExecutionReportStatus());

        Assertions.assertEquals(quantityLots, response.getLotsRequested());
        Assertions.assertEquals(quantityLots, response.getLotsExecuted());

        final MoneyValue expectedOrderPrice = TestData.createMoneyValue(totalPrice + totalCommissionAmount, currency);
        Assertions.assertEquals(expectedOrderPrice, response.getInitialOrderPrice());
        Assertions.assertEquals(expectedOrderPrice, response.getExecutedOrderPrice());

        final MoneyValue expectedTotalOrderAmount = TestData.createMoneyValue(totalPrice, currency);
        Assertions.assertEquals(expectedTotalOrderAmount, response.getTotalOrderAmount());

        final MoneyValue expectedCommission = TestData.createMoneyValue(totalCommissionAmount, currency);
        Assertions.assertEquals(expectedCommission, response.getInitialCommission());
        Assertions.assertEquals(expectedCommission, response.getExecutedCommission());

        Assertions.assertEquals(figi, response.getFigi());
        Assertions.assertEquals(direction, response.getDirection());

        final MoneyValue expectedInitialSecurityPrice = TestData.createMoneyValue(currentPrice, currency);
        Assertions.assertEquals(expectedInitialSecurityPrice, response.getInitialSecurityPrice());

        Assertions.assertEquals(type, response.getOrderType());
        Assertions.assertEquals(StringUtils.EMPTY, response.getOrderId());
    }

    @Test
    void createPostOrderResponse_whenOrderIdIsNotNull() {
        final Currency currency = Currency.EUR;
        final double totalPrice = 1000;
        final double totalCommissionAmount = 5;
        final double currentPrice = 100;
        final long quantityLots = 10;
        final String figi = "figi";
        final OrderDirection direction = OrderDirection.ORDER_DIRECTION_BUY;
        final OrderType type = OrderType.ORDER_TYPE_MARKET;
        final String orderId = "orderId";

        final PostOrderResponse response = DataStructsHelper.createPostOrderResponse(
                currency.name(),
                BigDecimal.valueOf(totalPrice),
                BigDecimal.valueOf(totalCommissionAmount),
                BigDecimal.valueOf(currentPrice),
                quantityLots,
                figi,
                direction,
                type,
                orderId
        );

        Assertions.assertEquals(OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_FILL, response.getExecutionReportStatus());

        Assertions.assertEquals(quantityLots, response.getLotsRequested());
        Assertions.assertEquals(quantityLots, response.getLotsExecuted());

        final MoneyValue expectedOrderPrice = TestData.createMoneyValue(totalPrice + totalCommissionAmount, currency);
        Assertions.assertEquals(expectedOrderPrice, response.getInitialOrderPrice());
        Assertions.assertEquals(expectedOrderPrice, response.getExecutedOrderPrice());

        final MoneyValue expectedTotalOrderAmount = TestData.createMoneyValue(totalPrice, currency);
        Assertions.assertEquals(expectedTotalOrderAmount, response.getTotalOrderAmount());

        final MoneyValue expectedCommission = TestData.createMoneyValue(totalCommissionAmount, currency);
        Assertions.assertEquals(expectedCommission, response.getInitialCommission());
        Assertions.assertEquals(expectedCommission, response.getExecutedCommission());

        Assertions.assertEquals(figi, response.getFigi());
        Assertions.assertEquals(direction, response.getDirection());

        final MoneyValue expectedInitialSecurityPrice = TestData.createMoneyValue(currentPrice, currency);
        Assertions.assertEquals(expectedInitialSecurityPrice, response.getInitialSecurityPrice());

        Assertions.assertEquals(type, response.getOrderType());
        Assertions.assertEquals(orderId, response.getOrderId());
    }

    // endregion

}