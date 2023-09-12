package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.Position;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootTest
@ActiveProfiles("test")
class FakeExtOperationsServiceIntegrationTest extends IntegrationTest {

    // region getOperations tests

    @Test
    void getOperations_filtersOperationsByInterval() {
        // arrange

        final String accountId1 = TestAccounts.IIS.account().id();
        final String accountId2 = TestAccounts.TINKOFF.account().id();

        final String figi = TestShares.SBER.share().figi();

        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(1);
        final OffsetDateTime dateTime3 = dateTime1.plusMinutes(2);

        final int price1 = 100;
        final int price2 = 200;
        final int price3 = 300;

        final long quantity1 = 1L;
        final long quantity2 = 2L;
        final long quantity3 = 3L;

        final Operation operation1 = TestData.newOperation(dateTime1, OperationType.OPERATION_TYPE_BUY, price1, quantity1, figi);
        final Operation operation2 = TestData.newOperation(dateTime2, OperationType.OPERATION_TYPE_BUY, price2, quantity2, figi);
        final Operation operation3 = TestData.newOperation(dateTime3, OperationType.OPERATION_TYPE_SELL, price3, quantity3, figi);
        final Operation operation4 = TestData.newOperation(dateTime1, OperationType.OPERATION_TYPE_BUY, price1, quantity1, figi);

        final FakeContext fakeContext = new FakeContext(dateTime1);
        fakeContext.addOperation(accountId1, operation1);
        fakeContext.addOperation(accountId1, operation2);
        fakeContext.addOperation(accountId1, operation3);
        fakeContext.addOperation(accountId2, operation4);

        final FakeExtOperationsService fakeExtOperationsService = new FakeExtOperationsService(fakeContext);

        final Interval wholeInterval = Interval.of(dateTime1, dateTime3);
        final Interval localInterval = Interval.of(dateTime1.plusMinutes(1), dateTime1.plusMinutes(1));

        // act

        final List<Operation> allOperations = fakeExtOperationsService.getOperations(accountId1, wholeInterval, figi);
        final List<Operation> localOperations = fakeExtOperationsService.getOperations(accountId1, localInterval, figi);

        // assert

        Assertions.assertEquals(Set.of(operation1, operation2, operation3), new HashSet<>(allOperations));
        Assertions.assertEquals(List.of(operation2), localOperations);
    }

    @Test
    void getOperations_filtersOperationsByFigi_whenFigiIsNotNull() {
        // arrange

        final String accountId1 = TestAccounts.IIS.account().id();
        final String accountId2 = TestAccounts.TINKOFF.account().id();

        final String figi1 = TestShares.SBER.share().figi();
        final String figi2 = TestShares.YANDEX.share().figi();

        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(1);
        final OffsetDateTime dateTime3 = dateTime1.plusMinutes(2);

        final int price1 = 100;
        final int price2 = 200;
        final int price3 = 300;

        final long quantity1 = 1L;
        final long quantity2 = 2L;
        final long quantity3 = 3L;

        final Operation operation1 = TestData.newOperation(dateTime1, OperationType.OPERATION_TYPE_BUY, price1, quantity1, figi1);
        final Operation operation2 = TestData.newOperation(dateTime2, OperationType.OPERATION_TYPE_BUY, price2, quantity2, figi2);
        final Operation operation3 = TestData.newOperation(dateTime3, OperationType.OPERATION_TYPE_SELL, price3, quantity3, figi2);
        final Operation operation4 = TestData.newOperation(dateTime1, OperationType.OPERATION_TYPE_BUY, price1, quantity1, figi2);

        final FakeContext fakeContext = new FakeContext(dateTime1);
        fakeContext.addOperation(accountId1, operation1);
        fakeContext.addOperation(accountId1, operation2);
        fakeContext.addOperation(accountId1, operation3);
        fakeContext.addOperation(accountId2, operation4);

        final FakeExtOperationsService fakeExtOperationsService = new FakeExtOperationsService(fakeContext);


        final Interval interval = Interval.of(dateTime1, dateTime1.plusMinutes(2));

        // act

        final List<Operation> figi1Operations = fakeExtOperationsService.getOperations(accountId1, interval, figi1);
        final List<Operation> figi2Operations = fakeExtOperationsService.getOperations(accountId1, interval, figi2);

        // assert

        AssertUtils.assertEquals(List.of(operation1), figi1Operations);
        AssertUtils.assertEquals(List.of(operation2, operation3), figi2Operations);
    }

    @Test
    void getOperations_doesNotFilterOperationsByFigi_whenFigiIsNull() {
        // arrange

        final String accountId1 = TestAccounts.IIS.account().id();
        final String accountId2 = TestAccounts.TINKOFF.account().id();

        final String figi1 = TestShares.SBER.share().figi();
        final String figi2 = TestShares.YANDEX.share().figi();
        final String figi3 = TestShares.APPLE.share().figi();

        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(1);
        final OffsetDateTime dateTime3 = dateTime1.plusMinutes(2);

        final int price1 = 100;
        final int price2 = 200;
        final int price3 = 300;

        final long quantity1 = 1L;
        final long quantity2 = 2L;
        final long quantity3 = 3L;

        final Operation operation1 = TestData.newOperation(dateTime1, OperationType.OPERATION_TYPE_BUY, price1, quantity1, figi1);
        final Operation operation2 = TestData.newOperation(dateTime2, OperationType.OPERATION_TYPE_BUY, price2, quantity2, figi2);
        final Operation operation3 = TestData.newOperation(dateTime3, OperationType.OPERATION_TYPE_SELL, price3, quantity3, figi2);
        final Operation operation4 = TestData.newOperation(dateTime1, OperationType.OPERATION_TYPE_BUY, price1, quantity1, figi3);

        final FakeContext fakeContext = new FakeContext(dateTime1);
        fakeContext.addOperation(accountId1, operation1);
        fakeContext.addOperation(accountId1, operation2);
        fakeContext.addOperation(accountId1, operation3);
        fakeContext.addOperation(accountId2, operation4);

        final FakeExtOperationsService fakeExtOperationsService = new FakeExtOperationsService(fakeContext);


        final Interval interval = Interval.of(dateTime1, dateTime1.plusMinutes(2));

        // act

        final List<Operation> actualOperations = fakeExtOperationsService.getOperations(accountId1, interval, null);

        // assert

        Assertions.assertEquals(List.of(operation1, operation2, operation3), actualOperations);
    }

    // endregion

    @Test
    void getPositions() {
        // arrange

        final String accountId1 = TestAccounts.IIS.account().id();
        final String accountId2 = TestAccounts.TINKOFF.account().id();

        final String figi1 = TestShares.SBER.share().figi();
        final String figi2 = TestShares.YANDEX.share().figi();
        final String figi3 = TestShares.APPLE.share().figi();

        final Position position1 = Position.builder().figi(figi1).build();
        final Position position2 = Position.builder().figi(figi2).build();
        final Position position3 = Position.builder().figi(figi2).build();

        final FakeContext fakeContext = new FakeContext(OffsetDateTime.now());
        fakeContext.addPosition(accountId1, figi1, position1);
        fakeContext.addPosition(accountId1, figi2, position2);
        fakeContext.addPosition(accountId2, figi3, position3);

        final FakeExtOperationsService fakeExtOperationsService = new FakeExtOperationsService(fakeContext);

        // act

        final List<Position> actualResult = fakeExtOperationsService.getPositions(accountId1);

        // assert

        final Set<Position> expectedResult = Set.of(position1, position2);
        Assertions.assertEquals(expectedResult, new HashSet<>(actualResult));
    }

    @Test
    void getWithdrawLimits() {
        // arrange

        final String accountId = TestAccounts.TINKOFF.account().id();

        final String currency1 = Currencies.USD;
        final String currency2 = Currencies.EUR;

        final FakeContext fakeContext = new FakeContext(OffsetDateTime.now());
        fakeContext.setBalance(accountId, currency1, DecimalUtils.setDefaultScale(2000L));
        fakeContext.setBalance(accountId, currency2, DecimalUtils.setDefaultScale(4000L));

        final FakeExtOperationsService fakeExtOperationsService = new FakeExtOperationsService(fakeContext);

        // act

        final WithdrawLimits withdrawLimits = fakeExtOperationsService.getWithdrawLimits(accountId);

        // assert

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

}