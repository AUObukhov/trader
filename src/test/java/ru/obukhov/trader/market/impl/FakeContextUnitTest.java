package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class FakeContextUnitTest {

    // region constructor tests

    @Test
    void constructor_withoutInitialBalances() {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        final FakeContext fakeContext = new FakeContext(currentDateTime);

        Assertions.assertEquals(currentDateTime, fakeContext.getCurrentDateTime());
    }

    @Test
    void constructor_withInitialBalances() {
        final String accountId = TestAccounts.TINKOFF.account().id();

        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        final String currency1 = Currencies.RUB;
        final String currency2 = Currencies.USD;
        final String currency3 = Currencies.EUR;

        final int balance1 = 100;
        final int balance2 = -100;
        final int balance3 = 0;

        final Map<String, BigDecimal> initialBalances = TestData.newDecimalMap(currency1, balance1, currency2, balance2, currency3, balance3);
        final FakeContext fakeContext = new FakeContext(accountId, currentDateTime, initialBalances);

        Assertions.assertEquals(currentDateTime, fakeContext.getCurrentDateTime());

        Assertions.assertEquals(1, fakeContext.getInvestments(accountId, currency1).size());
        Assertions.assertEquals(1, fakeContext.getInvestments(accountId, currency2).size());
        Assertions.assertEquals(1, fakeContext.getInvestments(accountId, currency3).size());

        AssertUtils.assertEquals(balance1, fakeContext.getBalance(accountId, currency1));
        AssertUtils.assertEquals(balance2, fakeContext.getBalance(accountId, currency2));
        AssertUtils.assertEquals(balance3, fakeContext.getBalance(accountId, currency3));
    }

    // endregion

    // region nextScheduleMinute tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forNextScheduleMinute() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 21, 6, 59, 59, 999999999),
                        DateTimeTestData.newDateTime(2023, 7, 21, 7, 0, 59, 999999999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 21, 6, 59, 30),
                        DateTimeTestData.newDateTime(2023, 7, 21, 7, 0, 30)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 21, 7),
                        DateTimeTestData.newDateTime(2023, 7, 21, 7, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 21, 14, 10),
                        DateTimeTestData.newDateTime(2023, 7, 21, 14, 11)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 21, 18, 58, 30),
                        DateTimeTestData.newDateTime(2023, 7, 21, 18, 59, 30)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 21, 18, 58, 59, 999999999),
                        DateTimeTestData.newDateTime(2023, 7, 21, 18, 59, 59, 999999999)
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 21, 18, 59),
                        DateTimeTestData.newDateTime(2023, 7, 24, 7)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 21, 19),
                        DateTimeTestData.newDateTime(2023, 7, 24, 7)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 21, 20),
                        DateTimeTestData.newDateTime(2023, 7, 24, 7)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 22, 8, 30),
                        DateTimeTestData.newDateTime(2023, 7, 24, 7)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 23, 13, 30),
                        DateTimeTestData.newDateTime(2023, 7, 24, 7)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 24, 6, 59, 30),
                        DateTimeTestData.newDateTime(2023, 7, 24, 7, 0, 30)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 24, 6, 59, 59, 999999999),
                        DateTimeTestData.newDateTime(2023, 7, 24, 7, 0, 59, 999999999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 24, 7),
                        DateTimeTestData.newDateTime(2023, 7, 24, 7, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 24, 14, 10),
                        DateTimeTestData.newDateTime(2023, 7, 24, 14, 11)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 24, 18, 58, 30),
                        DateTimeTestData.newDateTime(2023, 7, 24, 18, 59, 30)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 24, 18, 58, 59, 999999999),
                        DateTimeTestData.newDateTime(2023, 7, 24, 18, 59, 59, 999999999)
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 24, 18, 59),
                        DateTimeTestData.newDateTime(2023, 7, 25, 7)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 24, 18, 59, 30),
                        DateTimeTestData.newDateTime(2023, 7, 25, 7)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 24, 18, 59, 59, 99999999),
                        DateTimeTestData.newDateTime(2023, 7, 25, 7)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 24, 19),
                        DateTimeTestData.newDateTime(2023, 7, 25, 7)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 24, 20),
                        DateTimeTestData.newDateTime(2023, 7, 25, 7)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 25, 6, 59, 30),
                        DateTimeTestData.newDateTime(2023, 7, 25, 7, 0, 30)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 25, 6, 59, 59, 999999999),
                        DateTimeTestData.newDateTime(2023, 7, 25, 7, 0, 59, 999999999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 25, 7),
                        DateTimeTestData.newDateTime(2023, 7, 25, 7, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 25, 14, 10),
                        DateTimeTestData.newDateTime(2023, 7, 25, 14, 11)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 25, 18, 58, 30),
                        DateTimeTestData.newDateTime(2023, 7, 25, 18, 59, 30)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 25, 18, 58, 59, 999999999),
                        DateTimeTestData.newDateTime(2023, 7, 25, 18, 59, 59, 999999999)
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 25, 19),
                        null
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 25, 18, 59, 30),
                        null
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 25, 18, 59, 59, 99999999),
                        null
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 25, 18, 59),
                        null
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 25, 20),
                        null
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 7, 26, 7),
                        null
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forNextScheduleMinute")
    void nextScheduleMinute(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final List<TradingDay> tradingSchedule = TestData.newTradingSchedule(
                DateTimeTestData.newDateTime(2023, 7, 21, 7),
                DateTimeTestData.newTime(19, 0, 0),
                5
        );

        final FakeContext fakeContext = getFakeContext(dateTime, TestAccounts.TINKOFF.account().id(), Currencies.USD, DecimalUtils.ZERO);

        final OffsetDateTime actualResult = fakeContext.nextScheduleMinute(tradingSchedule);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    @Test
    void getBalances() {
        // arrange

        final String accountId1 = TestAccounts.TINKOFF.account().id();
        final String accountId2 = TestAccounts.IIS.account().id();

        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        final String currency1 = Currencies.RUB;
        final BigDecimal balance1 = DecimalUtils.setDefaultScale(100);
        final BigDecimal investment11 = DecimalUtils.setDefaultScale(20);
        final BigDecimal investment12 = DecimalUtils.setDefaultScale(50);
        final BigDecimal investment13 = DecimalUtils.setDefaultScale(30);
        final OffsetDateTime investment11DateTime = currentDateTime.plusHours(1);
        final OffsetDateTime investment12DateTime = investment11DateTime.plusHours(1);

        final FakeContext fakeContext = getFakeContext(currentDateTime, accountId1, currency1, balance1);

        fakeContext.addInvestment(accountId1, investment11DateTime, currency1, investment11);
        fakeContext.addInvestment(accountId1, investment12DateTime, currency1, investment12);
        fakeContext.addInvestment(accountId1, investment12DateTime, currency1, investment13);

        final String currency2 = Currencies.USD;
        final BigDecimal balance2 = DecimalUtils.setDefaultScale(1000);
        final BigDecimal investment21 = DecimalUtils.setDefaultScale(200);
        final BigDecimal investment22 = DecimalUtils.setDefaultScale(500);
        final BigDecimal investment23 = DecimalUtils.setDefaultScale(300);
        final OffsetDateTime investment21DateTime = currentDateTime.plusHours(2);
        final OffsetDateTime investment22DateTime = investment21DateTime.plusHours(1);

        fakeContext.setBalance(accountId2, currency2, balance2);
        fakeContext.addInvestment(accountId2, investment21DateTime, currency2, investment21);
        fakeContext.addInvestment(accountId2, investment22DateTime, currency2, investment22);
        fakeContext.addInvestment(accountId2, investment22DateTime, currency2, investment23);

        final String currency3 = Currencies.EUR;
        final BigDecimal balance3 = DecimalUtils.setDefaultScale(2000);
        final BigDecimal investment31 = DecimalUtils.setDefaultScale(400);
        final BigDecimal investment32 = DecimalUtils.setDefaultScale(1000);
        final BigDecimal investment33 = DecimalUtils.setDefaultScale(600);
        final OffsetDateTime investment31DateTime = currentDateTime.plusHours(2);
        final OffsetDateTime investment32DateTime = investment31DateTime.plusHours(1);

        fakeContext.setBalance(accountId2, currency3, balance3);
        fakeContext.addInvestment(accountId2, investment31DateTime, currency3, investment31);
        fakeContext.addInvestment(accountId2, investment32DateTime, currency3, investment32);
        fakeContext.addInvestment(accountId2, investment32DateTime, currency3, investment33);

        // action

        final Map<String, BigDecimal> balances = fakeContext.getBalances(accountId2);

        // assert

        Assertions.assertEquals(2, balances.size());
        Assertions.assertNull(balances.get(currency1));
        AssertUtils.assertEquals(2000, balances.get(currency2));
        AssertUtils.assertEquals(4000, balances.get(currency3));
    }

    // endregion

    @Test
    void addInvestment_changesInvestmentsAndCurrentBalance() {
        // arrange

        final String accountId = TestAccounts.TINKOFF.account().id();
        final String currency = Currencies.RUB;

        final BigDecimal balance = DecimalUtils.setDefaultScale(100);
        final BigDecimal investment1 = DecimalUtils.setDefaultScale(20);
        final BigDecimal investment2 = DecimalUtils.setDefaultScale(-50);
        final BigDecimal investment3 = DecimalUtils.setDefaultScale(0);

        final OffsetDateTime initialTimestamp = OffsetDateTime.now();
        final OffsetDateTime investment1Timestamp = initialTimestamp.plusHours(1);
        final OffsetDateTime investment2Timestamp = investment1Timestamp.plusHours(1);

        // action

        final FakeContext fakeContext = getFakeContext(initialTimestamp, accountId, currency, balance);

        // assert

        fakeContext.addInvestment(accountId, investment1Timestamp, currency, investment1);
        fakeContext.addInvestment(accountId, investment2Timestamp, currency, investment2);
        fakeContext.addInvestment(accountId, investment2Timestamp, currency, investment3);

        Assertions.assertEquals(3, fakeContext.getInvestments(accountId, currency).size());
        AssertUtils.assertEquals(balance, fakeContext.getInvestments(accountId, currency).get(initialTimestamp));
        AssertUtils.assertEquals(investment1, fakeContext.getInvestments(accountId, currency).get(investment1Timestamp));
        AssertUtils.assertEquals(investment2.add(investment3), fakeContext.getInvestments(accountId, currency).get(investment2Timestamp));

        final BigDecimal expectedBalance = balance.add(investment1).add(investment2).add(investment3);
        AssertUtils.assertEquals(expectedBalance, fakeContext.getBalance(accountId, currency));
    }

    @Test
    void addInvestments_changesInvestmentsAndCurrentBalance() {
        // arrange

        final String accountId = TestAccounts.TINKOFF.account().id();

        final String currency1 = Currencies.USD;
        final String currency2 = Currencies.RUB;

        final int balance1 = 100;
        final int balance2 = 0;

        final int investment11 = 20;
        final int investment12 = -50;
        final int investment13 = 0;

        final int investment21 = 200;
        final int investment22 = 500;
        final int investment23 = 300;

        final OffsetDateTime initialTimestamp = OffsetDateTime.now();
        final OffsetDateTime investment1Timestamp = initialTimestamp.plusHours(1);
        final OffsetDateTime investment2Timestamp = investment1Timestamp.plusHours(1);

        final Map<String, BigDecimal> initialBalances = TestData.newDecimalMap(currency1, balance1, currency2, balance2);

        // action

        final FakeContext fakeContext = new FakeContext(accountId, initialTimestamp, initialBalances);

        // assert

        final Map<String, BigDecimal> investments1 = TestData.newDecimalMap(currency1, investment11, currency2, investment21);
        final Map<String, BigDecimal> investments2 = TestData.newDecimalMap(currency1, investment12, currency2, investment22);
        final Map<String, BigDecimal> investments3 = TestData.newDecimalMap(currency1, investment13, currency2, investment23);

        fakeContext.addInvestments(accountId, investment1Timestamp, investments1);
        fakeContext.addInvestments(accountId, investment2Timestamp, investments2);
        fakeContext.addInvestments(accountId, investment2Timestamp, investments3);

        Assertions.assertEquals(3, fakeContext.getInvestments(accountId, currency1).size());
        Assertions.assertEquals(3, fakeContext.getInvestments(accountId, currency2).size());

        AssertUtils.assertEquals(balance1, fakeContext.getInvestments(accountId, currency1).get(initialTimestamp));
        AssertUtils.assertEquals(balance2, fakeContext.getInvestments(accountId, currency2).get(initialTimestamp));

        AssertUtils.assertEquals(investment11, fakeContext.getInvestments(accountId, currency1).get(investment1Timestamp));
        AssertUtils.assertEquals(investment21, fakeContext.getInvestments(accountId, currency2).get(investment1Timestamp));

        AssertUtils.assertEquals(investment12 + investment13, fakeContext.getInvestments(accountId, currency1).get(investment2Timestamp));
        AssertUtils.assertEquals(investment22 + investment23, fakeContext.getInvestments(accountId, currency2).get(investment2Timestamp));

        final int expectedBalance1 = balance1 + investment11 + investment12 + investment13;
        final int expectedBalance2 = balance2 + investment21 + investment22 + investment23;
        AssertUtils.assertEquals(expectedBalance1, fakeContext.getBalance(accountId, currency1));
        AssertUtils.assertEquals(expectedBalance2, fakeContext.getBalance(accountId, currency2));
    }

    @Test
    void addOperation_addsOperation_and_getOperationsReturnsOperations() {
        final String accountId = TestAccounts.TINKOFF.account().id();

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final String currency = Currencies.RUB;
        final BigDecimal balance = DecimalUtils.setDefaultScale(100L);

        final FakeContext fakeContext = getFakeContext(currentDateTime, accountId, currency, balance);

        final Operation operation = Operation.newBuilder()
                .setDate(DateTimeTestData.newTimestamp(2021, 1, 1, 10))
                .build();
        fakeContext.addOperation(accountId, operation);

        final Set<Operation> operations = fakeContext.getOperations(accountId);
        Assertions.assertEquals(1, operations.size());
        Assertions.assertSame(operation, operations.iterator().next());
    }

    @Test
    void addPosition_addsPosition_and_getPosition_returnsPosition() {
        final String accountId = TestAccounts.TINKOFF.account().id();

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final String currency = Currencies.RUB;
        final BigDecimal balance = DecimalUtils.setDefaultScale(100L);

        final FakeContext fakeContext = getFakeContext(currentDateTime, accountId, currency, balance);

        final String figi = TestShares.APPLE.share().figi();
        final Position position = new PositionBuilder().build();

        fakeContext.addPosition(accountId, figi, position);
        Position readPosition = fakeContext.getPosition(accountId, figi);

        Assertions.assertSame(position, readPosition);
    }

    @Test
    void getPositions_returnsAllPositions() {
        final String accountId = TestAccounts.TINKOFF.account().id();

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final String currency = Currencies.RUB;
        final BigDecimal balance = DecimalUtils.setDefaultScale(100L);

        final FakeContext fakeContext = getFakeContext(currentDateTime, accountId, currency, balance);

        final String figi1 = TestShares.APPLE.share().figi();
        final String figi2 = TestShares.SBER.share().figi();
        final Position position1 = new PositionBuilder().build();
        final Position position2 = new PositionBuilder().build();

        fakeContext.addPosition(accountId, figi1, position1);
        fakeContext.addPosition(accountId, figi2, position2);

        List<Position> positions = fakeContext.getPositions(accountId);

        Assertions.assertTrue(positions.contains(position1));
        Assertions.assertTrue(positions.contains(position2));
    }

    @Test
    void removePosition_removesPosition() {
        final String accountId = TestAccounts.TINKOFF.account().id();

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final String currency = Currencies.RUB;
        final BigDecimal balance = DecimalUtils.setDefaultScale(100);

        final FakeContext fakeContext = getFakeContext(currentDateTime, accountId, currency, balance);

        final String figi = TestShares.APPLE.share().figi();
        final Position position = new PositionBuilder().build();

        fakeContext.addPosition(accountId, figi, position);
        fakeContext.removePosition(accountId, figi);
        Assertions.assertTrue(fakeContext.getPositions(accountId).isEmpty());
    }

    @SuppressWarnings("SameParameterValue")
    private FakeContext getFakeContext(
            final OffsetDateTime currentDateTime,
            final String accountId,
            final String currency,
            final BigDecimal balance
    ) {
        final Map<String, BigDecimal> initialBalances = Map.of(currency, balance);
        return new FakeContext(accountId, currentDateTime, initialBalances);
    }

}