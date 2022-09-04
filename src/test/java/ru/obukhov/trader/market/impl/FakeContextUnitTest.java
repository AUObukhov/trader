package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.trading.model.BackTestOperation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class FakeContextUnitTest {

    private static final MarketProperties MARKET_PROPERTIES = TestData.createMarketProperties();

    // region constructor tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forConstructor() {
        return Stream.of(
                Arguments.of("2000124699", 100),
                Arguments.of("2000124699", -100),
                Arguments.of("2000124699", 0)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forConstructor")
    void constructor(final int balance) {
        final String accountId = "2000124699";

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;

        final FakeContext fakeContext = getFakeContext(currentDateTime, accountId, currency, DecimalUtils.setDefaultScale(balance));

        Assertions.assertEquals(currentDateTime, fakeContext.getCurrentDateTime());
        Assertions.assertEquals(1, fakeContext.getInvestments(accountId, currency).size());
        AssertUtils.assertEquals(balance, fakeContext.getBalance(accountId, currency));
    }

    // endregion

    // region nextMinute tests

    @Test
    void nextMinute_movesToNextMinute_whenMiddleOfWorkDay() {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);

        final FakeContext fakeContext = getFakeContext(dateTime, accountId, Currency.USD, BigDecimal.ZERO);

        final OffsetDateTime nextMinuteDateTime = fakeContext.nextMinute();

        final OffsetDateTime expected = dateTime.plusMinutes(1);
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, fakeContext.getCurrentDateTime());
    }

    @Test
    void nextMinute_movesToStartOfNextDay_whenAtEndOfWorkDay() {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 18, 59, 59);

        final FakeContext fakeContext = getFakeContext(dateTime, accountId, Currency.USD, BigDecimal.ZERO);

        final OffsetDateTime nextMinuteDateTime = fakeContext.nextMinute();

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), MARKET_PROPERTIES.getWorkSchedule().getStartTime());
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, fakeContext.getCurrentDateTime());
    }

    @Test
    void nextMinute_movesToStartOfNextWeek_whenEndOfWorkWeek() {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 9, 18, 59, 59);

        final FakeContext fakeContext = getFakeContext(dateTime, accountId, Currency.USD, BigDecimal.ZERO);

        final OffsetDateTime nextMinuteDateTime = fakeContext.nextMinute();

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(3), MARKET_PROPERTIES.getWorkSchedule().getStartTime());
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, fakeContext.getCurrentDateTime());
    }

    // endregion

    @Test
    void getBalances() {
        // arrange

        final String accountId1 = "2000124699";
        final String accountId2 = "2000124698";

        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        final Currency currency1 = Currency.RUB;
        final BigDecimal balance1 = BigDecimal.valueOf(100);
        final BigDecimal investment11 = BigDecimal.valueOf(20);
        final BigDecimal investment12 = BigDecimal.valueOf(50);
        final BigDecimal investment13 = BigDecimal.valueOf(30);
        final OffsetDateTime investment11DateTime = currentDateTime.plusHours(1);
        final OffsetDateTime investment12DateTime = investment11DateTime.plusHours(1);

        final FakeContext fakeContext = getFakeContext(currentDateTime, accountId1, currency1, balance1);

        fakeContext.setCurrentDateTime(investment11DateTime);
        fakeContext.addInvestment(accountId1, currency1, investment11);
        fakeContext.setCurrentDateTime(investment12DateTime);
        fakeContext.addInvestment(accountId1, currency1, investment12);
        fakeContext.addInvestment(accountId1, currency1, investment13);

        final Currency currency2 = Currency.USD;
        final BigDecimal balance2 = BigDecimal.valueOf(1000);
        final BigDecimal investment21 = BigDecimal.valueOf(200);
        final BigDecimal investment22 = BigDecimal.valueOf(500);
        final BigDecimal investment23 = BigDecimal.valueOf(300);
        final OffsetDateTime investment21DateTime = currentDateTime.plusHours(2);
        final OffsetDateTime investment22DateTime = investment21DateTime.plusHours(1);

        fakeContext.setBalance(accountId2, currency2, balance2);
        fakeContext.setCurrentDateTime(investment21DateTime);
        fakeContext.addInvestment(accountId2, currency2, investment21);
        fakeContext.setCurrentDateTime(investment22DateTime);
        fakeContext.addInvestment(accountId2, currency2, investment22);
        fakeContext.addInvestment(accountId2, currency2, investment23);

        final Currency currency3 = Currency.EUR;
        final BigDecimal balance3 = BigDecimal.valueOf(2000);
        final BigDecimal investment31 = BigDecimal.valueOf(400);
        final BigDecimal investment32 = BigDecimal.valueOf(1000);
        final BigDecimal investment33 = BigDecimal.valueOf(600);
        final OffsetDateTime investment31DateTime = currentDateTime.plusHours(2);
        final OffsetDateTime investment32DateTime = investment31DateTime.plusHours(1);

        fakeContext.setBalance(accountId2, currency3, balance3);
        fakeContext.setCurrentDateTime(investment31DateTime);
        fakeContext.addInvestment(accountId2, currency3, investment31);
        fakeContext.setCurrentDateTime(investment32DateTime);
        fakeContext.addInvestment(accountId2, currency3, investment32);
        fakeContext.addInvestment(accountId2, currency3, investment33);

        // action

        final Map<Currency, BigDecimal> balances = fakeContext.getBalances(accountId2);

        // assert

        Assertions.assertEquals(2, balances.size());
        Assertions.assertNull(balances.get(currency1));
        AssertUtils.assertEquals(2000, balances.get(currency2));
        AssertUtils.assertEquals(4000, balances.get(currency3));
    }

    // endregion

    // region addInvestment without dateTime tests

    @Test
    void addInvestment_withoutDateTime_changesInvestmentsAndCurrentBalance() {
        // arrange

        final String accountId = "2000124699";
        final Currency currency = Currency.RUB;

        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment1 = BigDecimal.valueOf(20);
        final BigDecimal investment2 = BigDecimal.valueOf(50);
        final BigDecimal investment3 = BigDecimal.valueOf(30);

        final OffsetDateTime initialDateTime = OffsetDateTime.now();
        final OffsetDateTime investment1DateTime = initialDateTime.plusHours(1);
        final OffsetDateTime investment2DateTime = investment1DateTime.plusHours(1);

        final FakeContext fakeContext = getFakeContext(initialDateTime, accountId, currency, balance);

        // action

        fakeContext.setCurrentDateTime(investment1DateTime);
        fakeContext.addInvestment(accountId, currency, investment1);

        fakeContext.setCurrentDateTime(investment2DateTime);
        fakeContext.addInvestment(accountId, currency, investment2);
        fakeContext.addInvestment(accountId, currency, investment3);

        // assert

        Assertions.assertEquals(3, fakeContext.getInvestments(accountId, currency).size());
        AssertUtils.assertEquals(balance, fakeContext.getInvestments(accountId, currency).get(initialDateTime));
        AssertUtils.assertEquals(investment1, fakeContext.getInvestments(accountId, currency).get(investment1DateTime));
        AssertUtils.assertEquals(investment2.add(investment3), fakeContext.getInvestments(accountId, currency).get(investment2DateTime));

        final BigDecimal expectedBalance = balance.add(investment1).add(investment2).add(investment3);
        AssertUtils.assertEquals(expectedBalance, fakeContext.getBalance(accountId, currency));
    }

    @Test
    void addInvestment_withoutDateTime_subtractsBalance_whenAmountIsNegative() {
        // arrange

        final String accountId = "2000124699";
        final Currency currency = Currency.RUB;

        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment = BigDecimal.valueOf(-20);

        final OffsetDateTime initialDateTime = OffsetDateTime.now();
        final OffsetDateTime investmentDateTime = initialDateTime.plusHours(1);

        final FakeContext fakeContext = getFakeContext(initialDateTime, accountId, currency, balance);

        fakeContext.setCurrentDateTime(investmentDateTime);

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

        final String accountId = "2000124699";
        final Currency currency = Currency.RUB;

        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment = BigDecimal.ZERO;

        final OffsetDateTime initialDateTime = OffsetDateTime.now();
        final OffsetDateTime investmentDateTime = initialDateTime.plusHours(1);

        final FakeContext fakeContext = getFakeContext(initialDateTime, accountId, currency, balance);

        fakeContext.setCurrentDateTime(investmentDateTime);

        // action

        fakeContext.addInvestment(accountId, currency, investment);

        // assert

        Assertions.assertEquals(2, fakeContext.getInvestments(accountId, currency).size());
        Assertions.assertEquals(balance, fakeContext.getInvestments(accountId, currency).get(initialDateTime));
        Assertions.assertEquals(investment, fakeContext.getInvestments(accountId, currency).get(investmentDateTime));

        AssertUtils.assertEquals(balance, fakeContext.getBalance(accountId, currency));
    }

    // endregion

    // region addInvestment with dateTime tests

    @Test
    void addInvestment_withDateTime_changesInvestmentsAndCurrentBalance() {
        // arrange

        final String accountId = "2000124699";
        final Currency currency = Currency.RUB;

        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment1 = BigDecimal.valueOf(20);
        final BigDecimal investment2 = BigDecimal.valueOf(50);
        final BigDecimal investment3 = BigDecimal.valueOf(30);

        final OffsetDateTime initialDateTime = OffsetDateTime.now();
        final OffsetDateTime investment1DateTime = initialDateTime.plusHours(1);
        final OffsetDateTime investment2DateTime = investment1DateTime.plusHours(1);

        // action

        final FakeContext fakeContext = getFakeContext(initialDateTime, accountId, currency, balance);

        // assert

        fakeContext.addInvestment(accountId, investment1DateTime, currency, investment1);
        fakeContext.addInvestment(accountId, investment2DateTime, currency, investment2);
        fakeContext.addInvestment(accountId, investment2DateTime, currency, investment3);

        Assertions.assertEquals(3, fakeContext.getInvestments(accountId, currency).size());
        AssertUtils.assertEquals(balance, fakeContext.getInvestments(accountId, currency).get(initialDateTime));
        AssertUtils.assertEquals(investment1, fakeContext.getInvestments(accountId, currency).get(investment1DateTime));
        AssertUtils.assertEquals(investment2.add(investment3), fakeContext.getInvestments(accountId, currency).get(investment2DateTime));

        final BigDecimal expectedBalance = balance.add(investment1).add(investment2).add(investment3);
        AssertUtils.assertEquals(expectedBalance, fakeContext.getBalance(accountId, currency));
    }

    @Test
    void addInvestment_withDateTime_throwsIllegalArgumentException_whenAmountIsNegative() {
        // arrange

        final String accountId = "2000124699";
        final Currency currency = Currency.RUB;

        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment = BigDecimal.valueOf(-20);

        final OffsetDateTime initialDateTime = OffsetDateTime.now();
        final OffsetDateTime investmentDateTime = initialDateTime.plusHours(1);

        final FakeContext fakeContext = getFakeContext(initialDateTime, accountId, currency, balance);

        fakeContext.setCurrentDateTime(investmentDateTime);

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

        final String accountId = "2000124699";
        final Currency currency = Currency.RUB;

        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment = BigDecimal.ZERO;

        final OffsetDateTime initialDateTime = OffsetDateTime.now();
        final OffsetDateTime investmentDateTime = initialDateTime.plusHours(1);

        final FakeContext fakeContext = getFakeContext(initialDateTime, accountId, currency, balance);

        fakeContext.setCurrentDateTime(investmentDateTime);

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
        final String accountId = "2000124699";

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);

        final FakeContext fakeContext = getFakeContext(currentDateTime, accountId, currency, balance);

        final BackTestOperation operation = new BackTestOperation(null,
                DateTimeTestData.createDateTime(2021, 1, 1, 10),
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
        final String accountId = "2000124699";

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);

        final FakeContext fakeContext = getFakeContext(currentDateTime, accountId, currency, balance);

        final String ticker = "ticker";
        PortfolioPosition position = TestData.createPortfolioPosition();

        fakeContext.addPosition(accountId, ticker, position);
        PortfolioPosition readPosition = fakeContext.getPosition(accountId, ticker);

        Assertions.assertSame(position, readPosition);
    }

    @Test
    void getPositions_returnsAllPositions() {
        final String accountId = "2000124699";

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);

        final FakeContext fakeContext = getFakeContext(currentDateTime, accountId, currency, balance);

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        PortfolioPosition position1 = TestData.createPortfolioPosition();
        PortfolioPosition position2 = TestData.createPortfolioPosition();

        fakeContext.addPosition(accountId, ticker1, position1);
        fakeContext.addPosition(accountId, ticker2, position2);

        List<PortfolioPosition> positions = fakeContext.getPositions(accountId);

        Assertions.assertTrue(positions.contains(position1));
        Assertions.assertTrue(positions.contains(position2));
    }

    @Test
    void removePosition_removesPosition() {
        final String accountId = "2000124699";

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);

        final FakeContext fakeContext = getFakeContext(currentDateTime, accountId, currency, balance);

        final String ticker = "ticker";
        PortfolioPosition position = TestData.createPortfolioPosition();

        fakeContext.addPosition(accountId, ticker, position);
        fakeContext.removePosition(accountId, ticker);
        Assertions.assertTrue(fakeContext.getPositions(accountId).isEmpty());
    }

    private FakeContext getFakeContext(
            final OffsetDateTime currentDateTime,
            final String accountId,
            final Currency currency,
            final BigDecimal balance
    ) {
        return new FakeContext(MARKET_PROPERTIES, currentDateTime, accountId, currency, balance);
    }

}