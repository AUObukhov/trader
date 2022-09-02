package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.trading.model.BackTestOperation;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class FakeExtOperationsServiceUnitTest {

    @Mock
    private FakeContext fakeContext;

    @InjectMocks
    private FakeExtOperationsService extOperationsService;

    // region getOperations tests

    @Test
    void getOperations_filtersOperationsByInterval() {
        // arrange

        final String accountId = "2000124699";
        final String ticker = "ticker";

        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(1);
        final OffsetDateTime dateTime3 = dateTime1.plusMinutes(2);

        final int price1 = 100;
        final int price2 = 200;
        final int price3 = 300;

        final long quantity1 = 1L;
        final long quantity2 = 2L;
        final long quantity3 = 3L;

        final BackTestOperation operation1 = new BackTestOperation(
                ticker,
                dateTime1,
                OperationType.OPERATION_TYPE_BUY,
                BigDecimal.valueOf(price1),
                quantity1
        );
        final BackTestOperation operation2 = new BackTestOperation(
                ticker,
                dateTime2,
                OperationType.OPERATION_TYPE_BUY,
                BigDecimal.valueOf(price2),
                quantity2
        );
        final BackTestOperation operation3 = new BackTestOperation(
                ticker,
                dateTime3,
                OperationType.OPERATION_TYPE_SELL,
                BigDecimal.valueOf(price3),
                quantity3
        );

        Mockito.when(fakeContext.getOperations(accountId)).thenReturn(Set.of(operation1, operation2, operation3));

        final Interval wholeInterval = Interval.of(dateTime1, dateTime3);
        final FakeExtOperationsService extOperationsService = new FakeExtOperationsService(fakeContext);

        // action

        final List<Operation> allOperations = extOperationsService.getOperations(accountId, wholeInterval, ticker);

        // assert

        final Operation expectedOperation1 = TestData.createOperation(dateTime1, OperationType.OPERATION_TYPE_BUY, price1, quantity1);
        final Operation expectedOperation2 = TestData.createOperation(dateTime2, OperationType.OPERATION_TYPE_BUY, price2, quantity2);
        final Operation expectedOperation3 = TestData.createOperation(dateTime3, OperationType.OPERATION_TYPE_SELL, price3, quantity3);

        AssertUtils.assertEquals(List.of(expectedOperation1, expectedOperation2, expectedOperation3), allOperations);

        final Interval localInterval = Interval.of(dateTime1.plusMinutes(1), dateTime1.plusMinutes(1));
        final List<Operation> localOperations = extOperationsService.getOperations(accountId, localInterval, ticker);
        AssertUtils.assertEquals(List.of(expectedOperation2), localOperations);
    }

    @Test
    void getOperations_filtersOperationsByTicker_whenTickerIsNotNull() {
        // arrange

        final String accountId = "2000124699";

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";

        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        ;
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(1);
        final OffsetDateTime dateTime3 = dateTime1.plusMinutes(2);

        final int price1 = 100;
        final int price2 = 200;
        final int price3 = 300;

        final long quantity1 = 1L;
        final long quantity2 = 2L;
        final long quantity3 = 3L;

        final BackTestOperation operation1 = new BackTestOperation(
                ticker1,
                dateTime1,
                OperationType.OPERATION_TYPE_BUY,
                BigDecimal.valueOf(price1),
                quantity1
        );
        final BackTestOperation operation2 = new BackTestOperation(
                ticker2,
                dateTime2,
                OperationType.OPERATION_TYPE_BUY,
                BigDecimal.valueOf(price2),
                quantity2
        );
        final BackTestOperation operation3 = new BackTestOperation(
                ticker2,
                dateTime3,
                OperationType.OPERATION_TYPE_SELL,
                BigDecimal.valueOf(price3),
                quantity3
        );

        Mockito.when(fakeContext.getOperations(accountId)).thenReturn(Set.of(operation1, operation2, operation3));

        final Interval interval = Interval.of(dateTime1, dateTime1.plusMinutes(2));

        // action

        final List<Operation> ticker1Operations = extOperationsService.getOperations(accountId, interval, ticker1);
        final List<Operation> ticker2Operations = extOperationsService.getOperations(accountId, interval, ticker2);

        // assert

        final Operation expectedTicker1Operation = TestData.createOperation(dateTime1, OperationType.OPERATION_TYPE_BUY, price1, quantity1);
        AssertUtils.assertEquals(List.of(expectedTicker1Operation), ticker1Operations);

        final Operation expectedTicker2Operation1 = TestData.createOperation(dateTime2, OperationType.OPERATION_TYPE_BUY, price2, quantity2);
        final Operation expectedTicker2Operation2 = TestData.createOperation(dateTime3, OperationType.OPERATION_TYPE_SELL, price3, quantity3);
        AssertUtils.assertEquals(List.of(expectedTicker2Operation1, expectedTicker2Operation2), ticker2Operations);
    }

    @Test
    void getOperations_doesNotFilterOperationsByTicker_whenTickerIsNull() {
        // arrange

        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";

        final OffsetDateTime dateTime1 = dateTime;
        final OffsetDateTime dateTime2 = dateTime.plusMinutes(1);
        final OffsetDateTime dateTime3 = dateTime.plusMinutes(2);

        final int price1 = 100;
        final int price2 = 200;
        final int price3 = 300;

        final long quantity1 = 1L;
        final long quantity2 = 2L;
        final long quantity3 = 3L;

        final BackTestOperation operation1 = new BackTestOperation(
                ticker1,
                dateTime1,
                OperationType.OPERATION_TYPE_BUY,
                BigDecimal.valueOf(price1),
                quantity1
        );
        final BackTestOperation operation2 = new BackTestOperation(
                ticker2,
                dateTime2,
                OperationType.OPERATION_TYPE_BUY,
                BigDecimal.valueOf(price2),
                quantity2
        );
        final BackTestOperation operation3 = new BackTestOperation(
                ticker2,
                dateTime3,
                OperationType.OPERATION_TYPE_SELL,
                BigDecimal.valueOf(price3),
                quantity3
        );

        Mockito.when(fakeContext.getOperations(accountId)).thenReturn(Set.of(operation1, operation2, operation3));

        final Interval interval = Interval.of(dateTime, dateTime.plusMinutes(2));

        // action

        final List<Operation> operations = extOperationsService.getOperations(accountId, interval, null);

        // assert

        final Operation expectedOperation1 = TestData.createOperation(
                dateTime1,
                OperationType.OPERATION_TYPE_BUY,
                price1,
                quantity1
        );
        final Operation expectedOperation2 = TestData.createOperation(
                dateTime2,
                OperationType.OPERATION_TYPE_BUY,
                price2,
                quantity2
        );
        final Operation expectedOperation3 = TestData.createOperation(
                dateTime3,
                OperationType.OPERATION_TYPE_SELL,
                price3,
                quantity3
        );
        AssertUtils.assertEquals(List.of(expectedOperation1, expectedOperation2, expectedOperation3), operations);
    }

    // endregion

    // region getWithdrawLimits tests

    @Test
    void getWithdrawLimits() {
        // arrange

        final String accountId = "2000124699";

        final Currency currency1 = Currency.USD;
        final Currency currency2 = Currency.EUR;

        final Map<Currency, BigDecimal> balances = Map.of(
                currency1, BigDecimal.valueOf(2000),
                currency2, BigDecimal.valueOf(4000)
        );
        Mockito.when(fakeContext.getBalances(accountId)).thenReturn(balances);

        // action

        final WithdrawLimits withdrawLimits = extOperationsService.getWithdrawLimits(accountId);

        //assert

        final List<Money> money = withdrawLimits.getMoney();
        Assertions.assertEquals(2, money.size());
        for (final Money currentMoney : money) {
            if (currentMoney.getCurrency().equals(Currency.USD.getJavaCurrency())) {
                AssertUtils.assertEquals(2000, currentMoney.getValue());
            } else if (currentMoney.getCurrency().equals(Currency.EUR.getJavaCurrency())) {
                AssertUtils.assertEquals(4000, currentMoney.getValue());
            } else {
                Assertions.fail("Unexpected currency: " + currentMoney.getCurrency());
            }
        }

        final List<Money> blocked = withdrawLimits.getBlocked();
        for (final Money currentMoney : blocked) {
            if (currentMoney.getCurrency().equals(Currency.USD.getJavaCurrency())) {
                AssertUtils.assertEquals(0, currentMoney.getValue());
            } else if (currentMoney.getCurrency().equals(Currency.EUR.getJavaCurrency())) {
                AssertUtils.assertEquals(0, currentMoney.getValue());
            } else {
                Assertions.fail("Unexpected currency: " + currentMoney.getCurrency());
            }
        }

        final List<Money> blockedGuarantee = withdrawLimits.getBlockedGuarantee();
        for (final Money currentMoney : blockedGuarantee) {
            if (currentMoney.getCurrency().equals(Currency.USD.getJavaCurrency())) {
                AssertUtils.assertEquals(0, currentMoney.getValue());
            } else if (currentMoney.getCurrency().equals(Currency.EUR.getJavaCurrency())) {
                AssertUtils.assertEquals(0, currentMoney.getValue());
            } else {
                Assertions.fail("Unexpected currency: " + currentMoney.getCurrency());
            }
        }
    }

    // endregion

}