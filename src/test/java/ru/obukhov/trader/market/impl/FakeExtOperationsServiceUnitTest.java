package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
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

        final String accountId = TestData.ACCOUNT_ID1;
        final String figi = TestShare1.FIGI;

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
                figi,
                dateTime1,
                OperationType.OPERATION_TYPE_BUY,
                DecimalUtils.setDefaultScale(price1),
                quantity1
        );
        final BackTestOperation operation2 = new BackTestOperation(
                figi,
                dateTime2,
                OperationType.OPERATION_TYPE_BUY,
                DecimalUtils.setDefaultScale(price2),
                quantity2
        );
        final BackTestOperation operation3 = new BackTestOperation(
                figi,
                dateTime3,
                OperationType.OPERATION_TYPE_SELL,
                DecimalUtils.setDefaultScale(price3),
                quantity3
        );

        Mockito.when(fakeContext.getOperations(accountId)).thenReturn(Set.of(operation1, operation2, operation3));

        final Interval wholeInterval = Interval.of(dateTime1, dateTime3);
        final FakeExtOperationsService extOperationsService = new FakeExtOperationsService(fakeContext);

        // action

        final List<Operation> allOperations = extOperationsService.getOperations(accountId, wholeInterval, figi);

        // assert

        final Operation expectedOperation1 = TestData.createOperation(dateTime1, OperationType.OPERATION_TYPE_BUY, price1, quantity1, figi);
        final Operation expectedOperation2 = TestData.createOperation(dateTime2, OperationType.OPERATION_TYPE_BUY, price2, quantity2, figi);
        final Operation expectedOperation3 = TestData.createOperation(dateTime3, OperationType.OPERATION_TYPE_SELL, price3, quantity3, figi);

        AssertUtils.assertEquals(List.of(expectedOperation1, expectedOperation2, expectedOperation3), allOperations);

        final Interval localInterval = Interval.of(dateTime1.plusMinutes(1), dateTime1.plusMinutes(1));
        final List<Operation> localOperations = extOperationsService.getOperations(accountId, localInterval, figi);
        AssertUtils.assertEquals(List.of(expectedOperation2), localOperations);
    }

    @Test
    void getOperations_filtersOperationsByFigi_whenFigiIsNotNull() {
        // arrange

        final String accountId = TestData.ACCOUNT_ID1;

        final String figi1 = TestShare1.FIGI;
        final String figi2 = TestShare2.FIGI;

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
                figi1,
                dateTime1,
                OperationType.OPERATION_TYPE_BUY,
                DecimalUtils.setDefaultScale(price1),
                quantity1
        );
        final BackTestOperation operation2 = new BackTestOperation(
                figi2,
                dateTime2,
                OperationType.OPERATION_TYPE_BUY,
                DecimalUtils.setDefaultScale(price2),
                quantity2
        );
        final BackTestOperation operation3 = new BackTestOperation(
                figi2,
                dateTime3,
                OperationType.OPERATION_TYPE_SELL,
                DecimalUtils.setDefaultScale(price3),
                quantity3
        );

        Mockito.when(fakeContext.getOperations(accountId)).thenReturn(Set.of(operation1, operation2, operation3));

        final Interval interval = Interval.of(dateTime1, dateTime1.plusMinutes(2));

        // action

        final List<Operation> figi1Operations = extOperationsService.getOperations(accountId, interval, figi1);
        final List<Operation> figi2Operations = extOperationsService.getOperations(accountId, interval, figi2);

        // assert

        final Operation expectedFigi1Operation = TestData.createOperation(dateTime1, OperationType.OPERATION_TYPE_BUY, price1, quantity1, figi1);
        AssertUtils.assertEquals(List.of(expectedFigi1Operation), figi1Operations);

        final Operation expectedFigi2Operation1 = TestData.createOperation(dateTime2, OperationType.OPERATION_TYPE_BUY, price2, quantity2, figi2);
        final Operation expectedFigi2Operation2 = TestData.createOperation(dateTime3, OperationType.OPERATION_TYPE_SELL, price3, quantity3, figi2);
        AssertUtils.assertEquals(List.of(expectedFigi2Operation1, expectedFigi2Operation2), figi2Operations);
    }

    @Test
    void getOperations_doesNotFilterOperationsByFigi_whenFigiIsNull() {
        // arrange

        final String accountId = TestData.ACCOUNT_ID1;

        final String figi1 = TestShare1.FIGI;
        final String figi2 = TestShare2.FIGI;

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
                figi1,
                dateTime1,
                OperationType.OPERATION_TYPE_BUY,
                DecimalUtils.setDefaultScale(price1),
                quantity1
        );
        final BackTestOperation operation2 = new BackTestOperation(
                figi2,
                dateTime2,
                OperationType.OPERATION_TYPE_BUY,
                DecimalUtils.setDefaultScale(price2),
                quantity2
        );
        final BackTestOperation operation3 = new BackTestOperation(
                figi2,
                dateTime3,
                OperationType.OPERATION_TYPE_SELL,
                DecimalUtils.setDefaultScale(price3),
                quantity3
        );

        Mockito.when(fakeContext.getOperations(accountId)).thenReturn(Set.of(operation1, operation2, operation3));

        final Interval interval = Interval.of(dateTime1, dateTime1.plusMinutes(2));

        // action

        final List<Operation> operations = extOperationsService.getOperations(accountId, interval, null);

        // assert

        final Operation expectedOperation1 = TestData.createOperation(
                dateTime1,
                OperationType.OPERATION_TYPE_BUY,
                price1,
                quantity1,
                figi1
        );
        final Operation expectedOperation2 = TestData.createOperation(
                dateTime2,
                OperationType.OPERATION_TYPE_BUY,
                price2,
                quantity2,
                figi2
        );
        final Operation expectedOperation3 = TestData.createOperation(
                dateTime3,
                OperationType.OPERATION_TYPE_SELL,
                price3,
                quantity3,
                figi2
        );
        AssertUtils.assertEquals(List.of(expectedOperation1, expectedOperation2, expectedOperation3), operations);
    }

    // endregion

    // region getWithdrawLimits tests

    @Test
    void getWithdrawLimits() {
        // arrange

        final String accountId = TestData.ACCOUNT_ID1;

        final String currency1 = Currency.USD;
        final String currency2 = Currency.EUR;

        final Map<String, BigDecimal> balances = Map.of(
                currency1, DecimalUtils.setDefaultScale(2000),
                currency2, DecimalUtils.setDefaultScale(4000)
        );
        Mockito.when(fakeContext.getBalances(accountId)).thenReturn(balances);

        // action

        final WithdrawLimits withdrawLimits = extOperationsService.getWithdrawLimits(accountId);

        //assert

        final List<Money> money = withdrawLimits.getMoney();
        Assertions.assertEquals(2, money.size());
        for (final Money currentMoney : money) {
            if (currentMoney.getCurrency().equals(Currency.USD)) {
                AssertUtils.assertEquals(2000, currentMoney.getValue());
            } else if (currentMoney.getCurrency().equals(Currency.EUR)) {
                AssertUtils.assertEquals(4000, currentMoney.getValue());
            } else {
                Assertions.fail("Unexpected currency: " + currentMoney.getCurrency());
            }
        }

        final List<Money> blocked = withdrawLimits.getBlocked();
        for (final Money currentMoney : blocked) {
            if (currentMoney.getCurrency().equals(Currency.USD)) {
                AssertUtils.assertEquals(0, currentMoney.getValue());
            } else if (currentMoney.getCurrency().equals(Currency.EUR)) {
                AssertUtils.assertEquals(0, currentMoney.getValue());
            } else {
                Assertions.fail("Unexpected currency: " + currentMoney.getCurrency());
            }
        }

        final List<Money> blockedGuarantee = withdrawLimits.getBlockedGuarantee();
        for (final Money currentMoney : blockedGuarantee) {
            if (currentMoney.getCurrency().equals(Currency.USD)) {
                AssertUtils.assertEquals(0, currentMoney.getValue());
            } else if (currentMoney.getCurrency().equals(Currency.EUR)) {
                AssertUtils.assertEquals(0, currentMoney.getValue());
            } else {
                Assertions.fail("Unexpected currency: " + currentMoney.getCurrency());
            }
        }
    }

    // endregion

}