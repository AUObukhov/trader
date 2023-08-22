package ru.obukhov.trader.market.impl;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

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

        final Timestamp timestamp1 = TimestampUtils.newTimestamp(2020, 10, 5, 12);
        final Timestamp timestamp2 = TimestampUtils.plusMinutes(timestamp1, 1);
        final Timestamp timestamp3 = TimestampUtils.plusMinutes(timestamp1, 2);

        final int price1 = 100;
        final int price2 = 200;
        final int price3 = 300;

        final long quantity1 = 1L;
        final long quantity2 = 2L;
        final long quantity3 = 3L;

        final Operation operation1 = Operation.newBuilder()
                .setFigi(figi)
                .setDate(timestamp1)
                .setOperationType(OperationType.OPERATION_TYPE_BUY)
                .setPrice(TestData.createMoneyValue(price1, ""))
                .setQuantity(quantity1)
                .build();
        final Operation operation2 = Operation.newBuilder()
                .setFigi(figi)
                .setDate(timestamp2)
                .setOperationType(OperationType.OPERATION_TYPE_BUY)
                .setPrice(TestData.createMoneyValue(price2, ""))
                .setQuantity(quantity2)
                .build();
        final Operation operation3 = Operation.newBuilder()
                .setFigi(figi)
                .setDate(timestamp3)
                .setOperationType(OperationType.OPERATION_TYPE_SELL)
                .setPrice(TestData.createMoneyValue(price3, ""))
                .setQuantity(quantity3)
                .build();

        Mockito.when(fakeContext.getOperations(accountId)).thenReturn(Set.of(operation1, operation2, operation3));

        final Interval wholeInterval = Interval.of(timestamp1, timestamp3);
        final FakeExtOperationsService extOperationsService = new FakeExtOperationsService(fakeContext);

        // action

        final List<Operation> allOperations = extOperationsService.getOperations(accountId, wholeInterval, figi);

        // assert

        final Operation expectedOperation1 = TestData.createOperation(timestamp1, OperationType.OPERATION_TYPE_BUY, price1, quantity1, figi);
        final Operation expectedOperation2 = TestData.createOperation(timestamp2, OperationType.OPERATION_TYPE_BUY, price2, quantity2, figi);
        final Operation expectedOperation3 = TestData.createOperation(timestamp3, OperationType.OPERATION_TYPE_SELL, price3, quantity3, figi);

        AssertUtils.assertEquals(List.of(expectedOperation1, expectedOperation2, expectedOperation3), allOperations);

        final Interval localInterval = Interval.of(TimestampUtils.plusMinutes(timestamp1, 1), TimestampUtils.plusMinutes(timestamp1, 1));
        final List<Operation> localOperations = extOperationsService.getOperations(accountId, localInterval, figi);
        AssertUtils.assertEquals(List.of(expectedOperation2), localOperations);
    }

    @Test
    void getOperations_filtersOperationsByFigi_whenFigiIsNotNull() {
        // arrange

        final String accountId = TestData.ACCOUNT_ID1;

        final String figi1 = TestShare1.FIGI;
        final String figi2 = TestShare2.FIGI;

        final Timestamp timestamp1 = TimestampUtils.newTimestamp(2020, 10, 5, 12);
        final Timestamp timestamp2 = TimestampUtils.plusMinutes(timestamp1, 1);
        final Timestamp timestamp3 = TimestampUtils.plusMinutes(timestamp1, 2);

        final int price1 = 100;
        final int price2 = 200;
        final int price3 = 300;

        final long quantity1 = 1L;
        final long quantity2 = 2L;
        final long quantity3 = 3L;

        final Operation operation1 = Operation.newBuilder()
                .setFigi(figi1)
                .setDate(timestamp1)
                .setOperationType(OperationType.OPERATION_TYPE_BUY)
                .setPrice(TestData.createMoneyValue(price1, ""))
                .setQuantity(quantity1)
                .build();
        final Operation operation2 = Operation.newBuilder()
                .setFigi(figi2)
                .setDate(timestamp2)
                .setOperationType(OperationType.OPERATION_TYPE_BUY)
                .setPrice(TestData.createMoneyValue(price2, ""))
                .setQuantity(quantity2)
                .build();
        final Operation operation3 = Operation.newBuilder()
                .setFigi(figi2)
                .setDate(timestamp3)
                .setOperationType(OperationType.OPERATION_TYPE_SELL)
                .setPrice(TestData.createMoneyValue(price3, ""))
                .setQuantity(quantity3)
                .build();

        Mockito.when(fakeContext.getOperations(accountId)).thenReturn(Set.of(operation1, operation2, operation3));

        final Interval interval = Interval.of(timestamp1, TimestampUtils.plusMinutes(timestamp1, 2));

        // action

        final List<Operation> figi1Operations = extOperationsService.getOperations(accountId, interval, figi1);
        final List<Operation> figi2Operations = extOperationsService.getOperations(accountId, interval, figi2);

        // assert

        final Operation expectedFigi1Operation = TestData.createOperation(timestamp1, OperationType.OPERATION_TYPE_BUY, price1, quantity1, figi1);
        AssertUtils.assertEquals(List.of(expectedFigi1Operation), figi1Operations);

        final Operation expectedFigi2Operation1 = TestData.createOperation(timestamp2, OperationType.OPERATION_TYPE_BUY, price2, quantity2, figi2);
        final Operation expectedFigi2Operation2 = TestData.createOperation(timestamp3, OperationType.OPERATION_TYPE_SELL, price3, quantity3, figi2);
        AssertUtils.assertEquals(List.of(expectedFigi2Operation1, expectedFigi2Operation2), figi2Operations);
    }

    @Test
    void getOperations_doesNotFilterOperationsByFigi_whenFigiIsNull() {
        // arrange

        final String accountId = TestData.ACCOUNT_ID1;

        final String figi1 = TestShare1.FIGI;
        final String figi2 = TestShare2.FIGI;

        final Timestamp timestamp1 = TimestampUtils.newTimestamp(2020, 10, 5, 12);
        final Timestamp timestamp2 = TimestampUtils.plusMinutes(timestamp1, 1);
        final Timestamp timestamp3 = TimestampUtils.plusMinutes(timestamp1, 2);

        final int price1 = 100;
        final int price2 = 200;
        final int price3 = 300;

        final long quantity1 = 1L;
        final long quantity2 = 2L;
        final long quantity3 = 3L;

        final Operation operation1 = Operation.newBuilder()
                .setFigi(figi1)
                .setDate(timestamp1)
                .setOperationType(OperationType.OPERATION_TYPE_BUY)
                .setPrice(TestData.createMoneyValue(price1, ""))
                .setQuantity(quantity1)
                .build();
        final Operation operation2 = Operation.newBuilder()
                .setFigi(figi2)
                .setDate(timestamp2)
                .setOperationType(OperationType.OPERATION_TYPE_BUY)
                .setPrice(TestData.createMoneyValue(price2, ""))
                .setQuantity(quantity2)
                .build();
        final Operation operation3 = Operation.newBuilder()
                .setFigi(figi2)
                .setDate(timestamp3)
                .setOperationType(OperationType.OPERATION_TYPE_SELL)
                .setPrice(TestData.createMoneyValue(price3, ""))
                .setQuantity(quantity3)
                .build();

        Mockito.when(fakeContext.getOperations(accountId)).thenReturn(Set.of(operation1, operation2, operation3));

        final Interval interval = Interval.of(timestamp1, TimestampUtils.plusMinutes(timestamp1, 2));

        // action

        final List<Operation> operations = extOperationsService.getOperations(accountId, interval, null);

        // assert

        final Operation expectedOperation1 = TestData.createOperation(
                timestamp1,
                OperationType.OPERATION_TYPE_BUY,
                price1,
                quantity1,
                figi1
        );
        final Operation expectedOperation2 = TestData.createOperation(
                timestamp2,
                OperationType.OPERATION_TYPE_BUY,
                price2,
                quantity2,
                figi2
        );
        final Operation expectedOperation3 = TestData.createOperation(
                timestamp3,
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

        final String currency1 = Currencies.USD;
        final String currency2 = Currencies.EUR;

        final Map<String, Quotation> balances = Map.of(
                currency1, QuotationUtils.newQuotation(2000L),
                currency2, QuotationUtils.newQuotation(4000L)
        );
        Mockito.when(fakeContext.getBalances(accountId)).thenReturn(balances);

        // action

        final WithdrawLimits withdrawLimits = extOperationsService.getWithdrawLimits(accountId);

        //assert

        final List<Money> money = withdrawLimits.getMoney();
        Assertions.assertEquals(2, money.size());
        for (final Money currentMoney : money) {
            if (currentMoney.getCurrency().equals(Currencies.USD)) {
                AssertUtils.assertEquals(2000, currentMoney.getValue());
            } else if (currentMoney.getCurrency().equals(Currencies.EUR)) {
                AssertUtils.assertEquals(4000, currentMoney.getValue());
            } else {
                Assertions.fail("Unexpected currency: " + currentMoney.getCurrency());
            }
        }

        final List<Money> blocked = withdrawLimits.getBlocked();
        for (final Money currentMoney : blocked) {
            if (currentMoney.getCurrency().equals(Currencies.USD)) {
                AssertUtils.assertEquals(0, currentMoney.getValue());
            } else if (currentMoney.getCurrency().equals(Currencies.EUR)) {
                AssertUtils.assertEquals(0, currentMoney.getValue());
            } else {
                Assertions.fail("Unexpected currency: " + currentMoney.getCurrency());
            }
        }

        final List<Money> blockedGuarantee = withdrawLimits.getBlockedGuarantee();
        for (final Money currentMoney : blockedGuarantee) {
            if (currentMoney.getCurrency().equals(Currencies.USD)) {
                AssertUtils.assertEquals(0, currentMoney.getValue());
            } else if (currentMoney.getCurrency().equals(Currencies.EUR)) {
                AssertUtils.assertEquals(0, currentMoney.getValue());
            } else {
                Assertions.fail("Unexpected currency: " + currentMoney.getCurrency());
            }
        }
    }

    // endregion

}