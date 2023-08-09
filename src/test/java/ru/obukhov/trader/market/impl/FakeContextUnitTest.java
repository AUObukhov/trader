package ru.obukhov.trader.market.impl;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
import ru.obukhov.trader.trading.model.BackTestOperation;
import ru.tinkoff.piapi.contract.v1.TradingDay;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class FakeContextUnitTest {

    // region constructor tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forConstructor() {
        return Stream.of(
                Arguments.of(TestData.ACCOUNT_ID1, 100),
                Arguments.of(TestData.ACCOUNT_ID1, -100),
                Arguments.of(TestData.ACCOUNT_ID1, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forConstructor")
    void constructor(final int balance) {
        final String accountId = TestData.ACCOUNT_ID1;

        final Timestamp currentTimestamp = TimestampUtils.now();
        final String currency = Currencies.RUB;

        final FakeContext fakeContext = getFakeContext(currentTimestamp, accountId, currency, DecimalUtils.setDefaultScale(balance));

        Assertions.assertEquals(currentTimestamp, fakeContext.getCurrentTimestamp());
        Assertions.assertEquals(1, fakeContext.getInvestments(accountId, currency).size());
        AssertUtils.assertEquals(balance, fakeContext.getBalance(accountId, currency));
    }

    // endregion

    // region nextScheduleMinute tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forNextScheduleMinute() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 6, 59, 59, 999999999),
                        TimestampUtils.newTimestamp(2023, 7, 21, 7, 0, 59, 999999999)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 6, 59, 30),
                        TimestampUtils.newTimestamp(2023, 7, 21, 7, 0, 30)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 7),
                        TimestampUtils.newTimestamp(2023, 7, 21, 7, 1)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 14, 10),
                        TimestampUtils.newTimestamp(2023, 7, 21, 14, 11)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 18, 58, 30),
                        TimestampUtils.newTimestamp(2023, 7, 21, 18, 59, 30)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 18, 58, 59, 999999999),
                        TimestampUtils.newTimestamp(2023, 7, 21, 18, 59, 59, 999999999)
                ),

                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 18, 59),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 19),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 20),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 22, 8, 30),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 23, 13, 30),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 6, 59, 30),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7, 0, 30)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 6, 59, 59, 999999999),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7, 0, 59, 999999999)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 7),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7, 1)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 14, 10),
                        TimestampUtils.newTimestamp(2023, 7, 24, 14, 11)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 58, 30),
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 59, 30)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 58, 59, 999999999),
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 59, 59, 999999999)
                ),

                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 59),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 59, 30),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 59, 59, 99999999),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 19),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 20),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 6, 59, 30),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7, 0, 30)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 6, 59, 59, 999999999),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7, 0, 59, 999999999)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 7),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7, 1)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 14, 10),
                        TimestampUtils.newTimestamp(2023, 7, 25, 14, 11)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 58, 30),
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 59, 30)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 58, 59, 999999999),
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 59, 59, 999999999)
                ),

                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 19),
                        null
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 59, 30),
                        null
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 59, 59, 99999999),
                        null
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 59),
                        null
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 20),
                        null
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 26, 7),
                        null
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forNextScheduleMinute")
    void nextScheduleMinute(final Timestamp timestamp, final Timestamp expectedResult) {
        final List<TradingDay> tradingSchedule = TestData.createTradingSchedule(
                TimestampUtils.newTimestamp(2023, 7, 21, 7),
                DateTimeTestData.createTime(19, 0, 0),
                5
        );

        final FakeContext fakeContext = getFakeContext(timestamp, TestData.ACCOUNT_ID1, Currencies.USD, DecimalUtils.setDefaultScale(0));

        final Timestamp actualResult = fakeContext.nextScheduleMinute(tradingSchedule);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    @Test
    void getBalances() {
        // arrange

        final String accountId1 = TestData.ACCOUNT_ID1;
        final String accountId2 = TestData.ACCOUNT_ID2;

        final Timestamp currentTimestamp = TimestampUtils.now();

        final String currency1 = Currencies.RUB;
        final BigDecimal balance1 = DecimalUtils.setDefaultScale(100);
        final BigDecimal investment11 = DecimalUtils.setDefaultScale(20);
        final BigDecimal investment12 = DecimalUtils.setDefaultScale(50);
        final BigDecimal investment13 = DecimalUtils.setDefaultScale(30);
        final Timestamp investment11DateTime = TimestampUtils.plusHours(currentTimestamp, 1);
        final Timestamp investment12DateTime = TimestampUtils.plusHours(investment11DateTime, 1);

        final FakeContext fakeContext = getFakeContext(currentTimestamp, accountId1, currency1, balance1);

        fakeContext.setCurrentTimestamp(investment11DateTime);
        fakeContext.addInvestment(accountId1, currency1, investment11);
        fakeContext.setCurrentTimestamp(investment12DateTime);
        fakeContext.addInvestment(accountId1, currency1, investment12);
        fakeContext.addInvestment(accountId1, currency1, investment13);

        final String currency2 = Currencies.USD;
        final BigDecimal balance2 = DecimalUtils.setDefaultScale(1000);
        final BigDecimal investment21 = DecimalUtils.setDefaultScale(200);
        final BigDecimal investment22 = DecimalUtils.setDefaultScale(500);
        final BigDecimal investment23 = DecimalUtils.setDefaultScale(300);
        final Timestamp investment21DateTime = TimestampUtils.plusHours(currentTimestamp, 2);
        final Timestamp investment22DateTime = TimestampUtils.plusHours(investment21DateTime, 1);

        fakeContext.setBalance(accountId2, currency2, balance2);
        fakeContext.setCurrentTimestamp(investment21DateTime);
        fakeContext.addInvestment(accountId2, currency2, investment21);
        fakeContext.setCurrentTimestamp(investment22DateTime);
        fakeContext.addInvestment(accountId2, currency2, investment22);
        fakeContext.addInvestment(accountId2, currency2, investment23);

        final String currency3 = Currencies.EUR;
        final BigDecimal balance3 = DecimalUtils.setDefaultScale(2000);
        final BigDecimal investment31 = DecimalUtils.setDefaultScale(400);
        final BigDecimal investment32 = DecimalUtils.setDefaultScale(1000);
        final BigDecimal investment33 = DecimalUtils.setDefaultScale(600);
        final Timestamp investment31DateTime = TimestampUtils.plusHours(currentTimestamp, 2);
        final Timestamp investment32DateTime = TimestampUtils.plusHours(investment31DateTime, 1);

        fakeContext.setBalance(accountId2, currency3, balance3);
        fakeContext.setCurrentTimestamp(investment31DateTime);
        fakeContext.addInvestment(accountId2, currency3, investment31);
        fakeContext.setCurrentTimestamp(investment32DateTime);
        fakeContext.addInvestment(accountId2, currency3, investment32);
        fakeContext.addInvestment(accountId2, currency3, investment33);

        // action

        final Map<String, BigDecimal> balances = fakeContext.getBalances(accountId2);

        // assert

        Assertions.assertEquals(2, balances.size());
        Assertions.assertNull(balances.get(currency1));
        AssertUtils.assertEquals(2000, balances.get(currency2));
        AssertUtils.assertEquals(4000, balances.get(currency3));
    }

    // endregion

    // region addInvestment without timestamp tests

    @Test
    void addInvestment_withoutTimestamp_changesInvestmentsAndCurrentBalance() {
        // arrange

        final String accountId = TestData.ACCOUNT_ID1;
        final String currency = Currencies.RUB;

        final BigDecimal balance = DecimalUtils.setDefaultScale(100);
        final BigDecimal investment1 = DecimalUtils.setDefaultScale(20);
        final BigDecimal investment2 = DecimalUtils.setDefaultScale(50);
        final BigDecimal investment3 = DecimalUtils.setDefaultScale(30);

        final Timestamp initialTimestamp = TimestampUtils.now();
        final Timestamp investment1Timestamp = TimestampUtils.plusHours(initialTimestamp, 1);
        final Timestamp investment2Timestamp = TimestampUtils.plusHours(investment1Timestamp, 1);

        final FakeContext fakeContext = getFakeContext(initialTimestamp, accountId, currency, balance);

        // action

        fakeContext.setCurrentTimestamp(investment1Timestamp);
        fakeContext.addInvestment(accountId, currency, investment1);

        fakeContext.setCurrentTimestamp(investment2Timestamp);
        fakeContext.addInvestment(accountId, currency, investment2);
        fakeContext.addInvestment(accountId, currency, investment3);

        // assert

        Assertions.assertEquals(3, fakeContext.getInvestments(accountId, currency).size());
        AssertUtils.assertEquals(balance, fakeContext.getInvestments(accountId, currency).get(initialTimestamp));
        AssertUtils.assertEquals(investment1, fakeContext.getInvestments(accountId, currency).get(investment1Timestamp));
        AssertUtils.assertEquals(investment2.add(investment3), fakeContext.getInvestments(accountId, currency).get(investment2Timestamp));

        final BigDecimal expectedBalance = balance.add(investment1).add(investment2).add(investment3);
        AssertUtils.assertEquals(expectedBalance, fakeContext.getBalance(accountId, currency));
    }

    @Test
    void addInvestment_withoutDateTime_subtractsBalance_whenAmountIsNegative() {
        // arrange

        final String accountId = TestData.ACCOUNT_ID1;
        final String currency = Currencies.RUB;

        final BigDecimal balance = DecimalUtils.setDefaultScale(100);
        final BigDecimal investment = DecimalUtils.setDefaultScale(-20);

        final Timestamp initialDateTime = TimestampUtils.now();

        final Timestamp investmentDateTime = TimestampUtils.plusHours(initialDateTime, 1);

        final FakeContext fakeContext = getFakeContext(initialDateTime, accountId, currency, balance);

        fakeContext.setCurrentTimestamp(investmentDateTime);

        // action

        fakeContext.addInvestment(accountId, currency, investment);

        // assert

        Assertions.assertEquals(2, fakeContext.getInvestments(accountId, currency).size());
        Assertions.assertEquals(balance, fakeContext.getInvestments(accountId, currency).get(initialDateTime));
        Assertions.assertEquals(investment, fakeContext.getInvestments(accountId, currency).get(investmentDateTime));

        AssertUtils.assertEquals(balance.add(investment), fakeContext.getBalance(accountId, currency));
    }

    @Test
    void addInvestment_withoutDateTime_notChangesBalance_whenAmountIsZero() {
        // arrange

        final String accountId = TestData.ACCOUNT_ID1;
        final String currency = Currencies.RUB;

        final BigDecimal balance = DecimalUtils.setDefaultScale(100);
        final BigDecimal investment = DecimalUtils.setDefaultScale(0);

        final Timestamp initialDateTime = TimestampUtils.now();
        final Timestamp investmentDateTime = TimestampUtils.plusHours(initialDateTime, 1);

        final FakeContext fakeContext = getFakeContext(initialDateTime, accountId, currency, balance);

        fakeContext.setCurrentTimestamp(investmentDateTime);

        // action

        fakeContext.addInvestment(accountId, currency, investment);

        // assert

        Assertions.assertEquals(2, fakeContext.getInvestments(accountId, currency).size());
        Assertions.assertEquals(balance, fakeContext.getInvestments(accountId, currency).get(initialDateTime));
        Assertions.assertEquals(investment, fakeContext.getInvestments(accountId, currency).get(investmentDateTime));

        AssertUtils.assertEquals(balance, fakeContext.getBalance(accountId, currency));
    }

    // endregion

    // region addInvestment with timestamp tests

    @Test
    void addInvestment_withTimestamp_changesInvestmentsAndCurrentBalance() {
        // arrange

        final String accountId = TestData.ACCOUNT_ID1;
        final String currency = Currencies.RUB;

        final BigDecimal balance = DecimalUtils.setDefaultScale(100);
        final BigDecimal investment1 = DecimalUtils.setDefaultScale(20);
        final BigDecimal investment2 = DecimalUtils.setDefaultScale(50);
        final BigDecimal investment3 = DecimalUtils.setDefaultScale(30);

        final Timestamp initialTimestamp = TimestampUtils.now();
        final Timestamp investment1Timestamp = TimestampUtils.plusHours(initialTimestamp, 1);
        final Timestamp investment2Timestamp = TimestampUtils.plusHours(investment1Timestamp, 1);

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
    void addInvestment_withDateTime_throwsIllegalArgumentException_whenAmountIsNegative() {
        // arrange

        final String accountId = TestData.ACCOUNT_ID1;
        final String currency = Currencies.RUB;

        final BigDecimal balance = DecimalUtils.setDefaultScale(100);
        final BigDecimal investment = DecimalUtils.setDefaultScale(-20);

        final Timestamp initialDateTime = TimestampUtils.now();
        final Timestamp investmentDateTime = TimestampUtils.plusHours(initialDateTime, 1);

        final FakeContext fakeContext = getFakeContext(initialDateTime, accountId, currency, balance);

        fakeContext.setCurrentTimestamp(investmentDateTime);

        // action

        fakeContext.addInvestment(accountId, investmentDateTime, currency, investment);

        // assert

        Assertions.assertEquals(2, fakeContext.getInvestments(accountId, currency).size());
        Assertions.assertEquals(balance, fakeContext.getInvestments(accountId, currency).get(initialDateTime));
        Assertions.assertEquals(investment, fakeContext.getInvestments(accountId, currency).get(investmentDateTime));

        AssertUtils.assertEquals(balance.add(investment), fakeContext.getBalance(accountId, currency));
    }

    @Test
    void addInvestment_withDateTime_throwsIllegalArgumentException_whenAmountIsZero() {
        // arrange

        final String accountId = TestData.ACCOUNT_ID1;
        final String currency = Currencies.RUB;

        final BigDecimal balance = DecimalUtils.setDefaultScale(100);
        final BigDecimal investment = DecimalUtils.setDefaultScale(0);

        final Timestamp initialDateTime = TimestampUtils.now();
        final Timestamp investmentDateTime = TimestampUtils.plusHours(initialDateTime, 1);

        final FakeContext fakeContext = getFakeContext(initialDateTime, accountId, currency, balance);

        fakeContext.setCurrentTimestamp(investmentDateTime);

        // action

        fakeContext.addInvestment(accountId, investmentDateTime, currency, investment);

        // assert

        Assertions.assertEquals(2, fakeContext.getInvestments(accountId, currency).size());
        Assertions.assertEquals(balance, fakeContext.getInvestments(accountId, currency).get(initialDateTime));
        Assertions.assertEquals(investment, fakeContext.getInvestments(accountId, currency).get(investmentDateTime));

        AssertUtils.assertEquals(balance, fakeContext.getBalance(accountId, currency));
    }

    // endregion

    @Test
    void addOperation_addsOperation_and_getOperationsReturnsOperations() {
        final String accountId = TestData.ACCOUNT_ID1;

        final Timestamp currentTimestamp = TimestampUtils.now();
        final String currency = Currencies.RUB;
        final BigDecimal balance = DecimalUtils.setDefaultScale(100);

        final FakeContext fakeContext = getFakeContext(currentTimestamp, accountId, currency, balance);

        final BackTestOperation operation = new BackTestOperation(null,
                TimestampUtils.newTimestamp(2021, 1, 1, 10),
                null,
                null,
                null
        );
        fakeContext.addOperation(accountId, operation);

        final Set<BackTestOperation> operations = fakeContext.getOperations(accountId);
        Assertions.assertEquals(1, operations.size());
        Assertions.assertSame(operation, operations.iterator().next());
    }

    @Test
    void addPosition_addsPosition_and_getPosition_returnsPosition() {
        final String accountId = TestData.ACCOUNT_ID1;

        final Timestamp currentTimestamp = TimestampUtils.now();
        final String currency = Currencies.RUB;
        final BigDecimal balance = DecimalUtils.setDefaultScale(100);

        final FakeContext fakeContext = getFakeContext(currentTimestamp, accountId, currency, balance);

        final String figi = TestShare1.FIGI;
        Position position = Position.builder().build();

        fakeContext.addPosition(accountId, figi, position);
        Position readPosition = fakeContext.getPosition(accountId, figi);

        Assertions.assertSame(position, readPosition);
    }

    @Test
    void getPositions_returnsAllPositions() {
        final String accountId = TestData.ACCOUNT_ID1;

        final Timestamp currentTimestamp = TimestampUtils.now();
        final String currency = Currencies.RUB;
        final BigDecimal balance = DecimalUtils.setDefaultScale(100);

        final FakeContext fakeContext = getFakeContext(currentTimestamp, accountId, currency, balance);

        final String figi1 = TestShare1.FIGI;
        final String figi2 = TestShare2.FIGI;
        Position position1 = Position.builder().build();
        Position position2 = Position.builder().build();

        fakeContext.addPosition(accountId, figi1, position1);
        fakeContext.addPosition(accountId, figi2, position2);

        List<Position> positions = fakeContext.getPositions(accountId);

        Assertions.assertTrue(positions.contains(position1));
        Assertions.assertTrue(positions.contains(position2));
    }

    @Test
    void removePosition_removesPosition() {
        final String accountId = TestData.ACCOUNT_ID1;

        final Timestamp currentTimestamp = TimestampUtils.now();
        final String currency = Currencies.RUB;
        final BigDecimal balance = DecimalUtils.setDefaultScale(100);

        final FakeContext fakeContext = getFakeContext(currentTimestamp, accountId, currency, balance);

        final String figi = TestShare1.FIGI;
        Position position = Position.builder().build();

        fakeContext.addPosition(accountId, figi, position);
        fakeContext.removePosition(accountId, figi);
        Assertions.assertTrue(fakeContext.getPositions(accountId).isEmpty());
    }

    @SuppressWarnings("SameParameterValue")
    private FakeContext getFakeContext(
            final Timestamp currentTimestamp,
            final String accountId,
            final String currency,
            final BigDecimal balance
    ) {
        return new FakeContext(currentTimestamp, accountId, currency, balance);
    }

}