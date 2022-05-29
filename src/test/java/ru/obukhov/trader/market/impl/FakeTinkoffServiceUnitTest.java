package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.MarketOrderRequest;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class FakeTinkoffServiceUnitTest {

    private static final MarketProperties MARKET_PROPERTIES = TestData.createMarketProperties();

    @Mock
    private MarketService marketService;
    @Mock
    private MarketInstrumentsService marketInstrumentsService;
    @Mock
    private RealTinkoffService realTinkoffService;
    @InjectMocks
    private TinkoffServices tinkoffServices;

    private FakeTinkoffService service;

    // region init tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forInit_movesCurrentDateTimeToCeilingWorkTime() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 12),
                        DateTimeTestData.createDateTime(2020, 10, 5, 12)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 19),
                        DateTimeTestData.createDateTime(2020, 10, 6, 10)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 19, 20),
                        DateTimeTestData.createDateTime(2020, 10, 6, 10)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 9, 19), // friday
                        DateTimeTestData.createDateTime(2020, 10, 12, 10) // monday
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 10, 12), // saturday
                        DateTimeTestData.createDateTime(2020, 10, 12, 10) // monday
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 9, 9),
                        DateTimeTestData.createDateTime(2020, 10, 9, 10)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forInit_movesCurrentDateTimeToCeilingWorkTime")
    void constructor_movesCurrentDateTimeToCeilingWorkTime(final OffsetDateTime dateTime, final OffsetDateTime expectedCurrentDateTime) {
        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                "2000124699",
                dateTime,
                Currency.USD,
                0.003,
                TestData.createBalanceConfig(0.0)
        );

        Assertions.assertEquals(expectedCurrentDateTime, service.getCurrentDateTime());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forConstructor_initsBalance() {
        return Stream.of(
                Arguments.of(TestData.createBalanceConfig(1000000.0, 1000.0, "0 0 0 1 * ?"), 1001000.0),
                Arguments.of(TestData.createBalanceConfig(1000000.0, 1000.0, "0 0 0 2 * ?"), 1000000.0),
                Arguments.of(TestData.createBalanceConfig(1000000.0), 1000000.0)
        );
    }

    @ParameterizedTest
    @MethodSource(value = "getData_forConstructor_initsBalance")
    void constructor_initsBalance(final BalanceConfig balanceConfig, final double expectedBalance) {
        constructor_initsBalance(null, balanceConfig, expectedBalance);
        constructor_initsBalance("2000124699", balanceConfig, expectedBalance);
    }

    private void constructor_initsBalance(final String accountId, final BalanceConfig balanceConfig, final double expectedBalance) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 1);
        final Currency currency = Currency.RUB;

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                accountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final BigDecimal balance = service.getCurrentBalance(accountId, currency);

        AssertUtils.assertEquals(expectedBalance, balance);
    }

    // endregion

    // region nextMinute tests

    @Test
    void nextMinute_movesToNextMinute_whenMiddleOfWorkDay() {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                accountId,
                dateTime,
                Currency.USD,
                0.003,
                TestData.createBalanceConfig(0.0)
        );

        final OffsetDateTime nextMinuteDateTime = service.nextMinute();

        final OffsetDateTime expected = dateTime.plusMinutes(1);
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, service.getCurrentDateTime());
    }

    @Test
    void nextMinute_movesToStartOfNextDay_whenAtEndOfWorkDay() {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 18, 59, 59);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                accountId,
                dateTime,
                Currency.USD,
                0.003,
                TestData.createBalanceConfig(0.0)
        );

        final OffsetDateTime nextMinuteDateTime = service.nextMinute();

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), MARKET_PROPERTIES.getWorkSchedule().getStartTime());
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, service.getCurrentDateTime());
    }

    @Test
    void nextMinute_movesToStartOfNextWeek_whenEndOfWorkWeek() {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 9, 18, 59, 59);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                accountId,
                dateTime,
                Currency.USD,
                0.003,
                TestData.createBalanceConfig(0.0)
        );

        final OffsetDateTime nextMinuteDateTime = service.nextMinute();

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(3), MARKET_PROPERTIES.getWorkSchedule().getStartTime());
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, service.getCurrentDateTime());
    }

    // endregion

    // region getOperations tests

    @Test
    void getOperations_filtersOperationsByInterval() throws IOException {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                accountId,
                dateTime,
                Currency.RUB,
                0.003,
                balanceConfig
        );

        final String ticker = "ticker";
        final int lotSize = 10;

        mockShare(ticker, Currency.RUB, lotSize);

        final OffsetDateTime operation1DateTime = service.getCurrentDateTime();
        placeMarketOrder(accountId, ticker, 1, OperationType.OPERATION_TYPE_BUY, 100);
        final OffsetDateTime operation2DateTime = service.nextMinute();
        placeMarketOrder(accountId, ticker, 1, OperationType.OPERATION_TYPE_BUY, 200);
        final OffsetDateTime operation3DateTime = service.nextMinute();
        placeMarketOrder(accountId, ticker, 1, OperationType.OPERATION_TYPE_SELL, 300);

        final Interval wholeInterval = Interval.of(dateTime, dateTime.plusMinutes(2));
        final List<Operation> allOperations = service.getOperations(accountId, wholeInterval, ticker);

        final Operation expectedOperation1 = TestData.createOperation(operation1DateTime, OperationType.OPERATION_TYPE_BUY, 100, 10);
        final Operation expectedOperation2 = TestData.createOperation(operation2DateTime, OperationType.OPERATION_TYPE_BUY, 200, 10);
        final Operation expectedOperation3 = TestData.createOperation(operation3DateTime, OperationType.OPERATION_TYPE_SELL, 300, 10);

        AssertUtils.assertEquals(List.of(expectedOperation1, expectedOperation2, expectedOperation3), allOperations);

        final Interval localInterval = Interval.of(dateTime.plusMinutes(1), dateTime.plusMinutes(1));
        final List<Operation> localOperations = service.getOperations(accountId, localInterval, ticker);
        AssertUtils.assertEquals(List.of(expectedOperation2), localOperations);
    }

    @Test
    void getOperations_filtersOperationsByTicker_whenTickerIsNotNull() throws IOException {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                accountId,
                dateTime,
                Currency.RUB,
                0.003,
                balanceConfig
        );

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final int lotSize = 10;

        mockShare(ticker1, Currency.RUB, lotSize);
        mockShare(ticker2, Currency.RUB, lotSize);

        final OffsetDateTime ticker1OperationDateTime = service.getCurrentDateTime();
        placeMarketOrder(accountId, ticker1, 1, OperationType.OPERATION_TYPE_BUY, 100);
        final OffsetDateTime ticker2Operation1DateTime = service.nextMinute();
        placeMarketOrder(accountId, ticker2, 1, OperationType.OPERATION_TYPE_BUY, 200);
        final OffsetDateTime ticker2Operation2DateTime = service.nextMinute();
        placeMarketOrder(accountId, ticker2, 1, OperationType.OPERATION_TYPE_SELL, 300);

        final Interval interval = Interval.of(dateTime, dateTime.plusMinutes(2));
        final List<Operation> ticker1Operations = service.getOperations(accountId, interval, ticker1);
        final List<Operation> ticker2Operations = service.getOperations(accountId, interval, ticker2);

        final Operation expectedTicker1Operation = TestData.createOperation(ticker1OperationDateTime, OperationType.OPERATION_TYPE_BUY, 100, 10);
        AssertUtils.assertEquals(List.of(expectedTicker1Operation), ticker1Operations);
        final Operation expectedTicker2Operation1 = TestData.createOperation(ticker2Operation1DateTime, OperationType.OPERATION_TYPE_BUY, 200, 10);
        final Operation expectedTicker2Operation2 = TestData.createOperation(ticker2Operation2DateTime, OperationType.OPERATION_TYPE_SELL, 300, 10);
        AssertUtils.assertEquals(List.of(expectedTicker2Operation1, expectedTicker2Operation2), ticker2Operations);
    }

    @Test
    void getOperations_doesNotFilterOperationsByTicker_whenTickerIsNull() throws IOException {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                accountId,
                dateTime,
                Currency.RUB,
                0.003,
                balanceConfig
        );

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final int lotSize = 10;

        mockShare(ticker1, Currency.RUB, lotSize);
        mockShare(ticker2, Currency.RUB, lotSize);

        final OffsetDateTime operation1DateTime = service.getCurrentDateTime();
        placeMarketOrder(accountId, ticker1, 1, OperationType.OPERATION_TYPE_BUY, 100);
        final OffsetDateTime operation2DateTime = service.nextMinute();
        placeMarketOrder(accountId, ticker2, 1, OperationType.OPERATION_TYPE_BUY, 200);
        final OffsetDateTime operation3DateTime = service.nextMinute();
        placeMarketOrder(accountId, ticker2, 1, OperationType.OPERATION_TYPE_SELL, 300);

        final Interval interval = Interval.of(dateTime, dateTime.plusMinutes(2));
        final List<Operation> operations = service.getOperations(accountId, interval, null);

        final Operation expectedOperation1 = TestData.createOperation(
                operation1DateTime,
                OperationType.OPERATION_TYPE_BUY,
                100,
                10
        );
        final Operation expectedOperation2 = TestData.createOperation(
                operation2DateTime,
                OperationType.OPERATION_TYPE_BUY,
                200,
                10
        );
        final Operation expectedOperation3 = TestData.createOperation(
                operation3DateTime,
                OperationType.OPERATION_TYPE_SELL,
                300,
                10
        );
        AssertUtils.assertEquals(List.of(expectedOperation1, expectedOperation2, expectedOperation3), operations);
    }

    // endregion

    // region placeMarketOrder tests. Implicit tests for getPortfolioPositions

    @Test
    void placeMarketOrder_buy_throwsIllegalArgumentException_whenNotEnoughBalance() {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000.0);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                accountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final String ticker = "ticker";

        mockShare(ticker, Currency.RUB, 10);

        final Executable executable = () -> placeMarketOrder(accountId, ticker, 2, OperationType.OPERATION_TYPE_BUY, 500);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "balance can't be negative");

        Assertions.assertTrue(service.getPortfolioPositions(accountId).isEmpty());
        AssertUtils.assertEquals(1000, service.getCurrentBalance(accountId, currency));
    }

    @Test
    void placeMarketOrder_buy_createsNewPosition_whenNoPositions() throws IOException {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                accountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final String ticker = "ticker";

        mockShare(ticker, Currency.RUB, 10);

        placeMarketOrder(accountId, ticker, 1, OperationType.OPERATION_TYPE_BUY, 1000);

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions(accountId);
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.ticker());
        AssertUtils.assertEquals(10, portfolioPosition.quantity());
        AssertUtils.assertEquals(989970, service.getCurrentBalance(accountId, currency));
    }

    @Test
    void placeMarketOrder_buy_addsValueToExistingPosition_whenPositionAlreadyExists() throws IOException {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                accountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final String ticker = "ticker";

        mockShare(ticker, Currency.RUB, 10);

        placeMarketOrder(accountId, ticker, 2, OperationType.OPERATION_TYPE_BUY, 1000);
        placeMarketOrder(accountId, ticker, 1, OperationType.OPERATION_TYPE_BUY, 4000);

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions(accountId);
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.ticker());
        AssertUtils.assertEquals(30, portfolioPosition.quantity());
        AssertUtils.assertEquals(2000, portfolioPosition.averagePositionPrice().value());
        AssertUtils.assertEquals(939820, service.getCurrentBalance(accountId, currency));
    }

    @Test
    void placeMarketOrder_buy_createsMultiplePositions_whenDifferentTickersAreBought() throws IOException {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                accountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final String ticker3 = "ticker3";

        mockShare(ticker1, Currency.RUB, 10);
        mockShare(ticker2, Currency.RUB, 2);
        mockShare(ticker3, Currency.RUB, 1);

        placeMarketOrder(accountId, ticker1, 1, OperationType.OPERATION_TYPE_BUY, 1000);
        placeMarketOrder(accountId, ticker2, 3, OperationType.OPERATION_TYPE_BUY, 100);
        placeMarketOrder(accountId, ticker3, 1, OperationType.OPERATION_TYPE_BUY, 500);

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions(accountId);
        Assertions.assertEquals(3, positions.size());

        final Iterator<PortfolioPosition> positionIterator = positions.iterator();
        final PortfolioPosition portfolioPosition1 = positionIterator.next();
        Assertions.assertEquals(ticker1, portfolioPosition1.ticker());
        AssertUtils.assertEquals(10, portfolioPosition1.quantity());

        final PortfolioPosition portfolioPosition2 = positionIterator.next();
        Assertions.assertEquals(ticker2, portfolioPosition2.ticker());
        AssertUtils.assertEquals(6, portfolioPosition2.quantity());

        final PortfolioPosition portfolioPosition3 = positionIterator.next();
        Assertions.assertEquals(ticker3, portfolioPosition3.ticker());
        AssertUtils.assertEquals(1, portfolioPosition3.quantity());

        AssertUtils.assertEquals(988866.7, service.getCurrentBalance(accountId, currency));
    }

    @Test
    void placeMarketOrder_sell_throwsIllegalArgumentException_whenSellsMoreLotsThanExists() throws IOException {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);
        final int lotSize = 10;

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                accountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final String ticker = "ticker";

        mockShare(ticker, Currency.RUB, lotSize);

        placeMarketOrder(accountId, ticker, 2, OperationType.OPERATION_TYPE_BUY, 1000);
        placeMarketOrder(accountId, ticker, 1, OperationType.OPERATION_TYPE_BUY, 4000);
        final Executable sellExecutable = () -> placeMarketOrder(accountId, ticker, 4, OperationType.OPERATION_TYPE_SELL, 3000);
        final String expectedMessage = "lotsCount 4 can't be greater than existing position lots count 3";
        Assertions.assertThrows(IllegalArgumentException.class, sellExecutable, expectedMessage);

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions(accountId);
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.ticker());
        AssertUtils.assertEquals(30, portfolioPosition.quantity());
        AssertUtils.assertEquals(2000, portfolioPosition.averagePositionPrice().value());
        AssertUtils.assertEquals(939820, service.getCurrentBalance(accountId, currency));
    }

    @Test
    void placeMarketOrder_sell_removesPosition_whenAllLotsAreSold() throws IOException {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);
        final int lotSize = 10;

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                accountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final String ticker = "ticker";

        mockShare(ticker, Currency.RUB, lotSize);

        placeMarketOrder(accountId, ticker, 2, OperationType.OPERATION_TYPE_BUY, 1000);
        placeMarketOrder(accountId, ticker, 1, OperationType.OPERATION_TYPE_BUY, 4000);
        placeMarketOrder(accountId, ticker, 3, OperationType.OPERATION_TYPE_SELL, 3000);

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions(accountId);
        Assertions.assertTrue(positions.isEmpty());
        AssertUtils.assertEquals(1029550, service.getCurrentBalance(accountId, currency));
    }

    @Test
    void placeMarketOrder_sell_reducesLotsCount() throws IOException {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);
        final int lotSize = 10;

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                accountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final String ticker = "ticker";

        mockShare(ticker, Currency.RUB, lotSize);

        placeMarketOrder(accountId, ticker, 2, OperationType.OPERATION_TYPE_BUY, 1000);
        placeMarketOrder(accountId, ticker, 1, OperationType.OPERATION_TYPE_BUY, 4000);
        placeMarketOrder(accountId, ticker, 1, OperationType.OPERATION_TYPE_SELL, 3000);

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions(accountId);
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.ticker());
        AssertUtils.assertEquals(20, portfolioPosition.quantity());
        AssertUtils.assertEquals(2000, portfolioPosition.averagePositionPrice().value());
        AssertUtils.assertEquals(969730, service.getCurrentBalance(accountId, currency));
    }

    // endregion

    // region getWithdrawLimits tests

    @Test
    void getWithdrawLimits_returnsAllCurrencies_whenCurrenciesAreNotInitialized() {
        final String accountId = "2000124699";

        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.USD;

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                accountId,
                dateTime,
                currency,
                0.003,
                TestData.createBalanceConfig(0.0)
        );

        final WithdrawLimits withdrawLimits = service.getWithdrawLimits(accountId);

        final List<Money> moneys = withdrawLimits.getMoney();
        Assertions.assertEquals(1, moneys.size());
        Assertions.assertEquals(currency.name(), moneys.get(0).getCurrency().getCurrencyCode());
        for (final Money money : moneys) {
            AssertUtils.assertEquals(0, money.getValue());
        }

        final List<Money> blocked = withdrawLimits.getBlocked();
        Assertions.assertEquals(1, blocked.size());
        Assertions.assertEquals(currency.name(), blocked.get(0).getCurrency().getCurrencyCode());
        for (final Money money : blocked) {
            AssertUtils.assertEquals(0, money.getValue());
        }

        final List<Money> blockedGuarantee = withdrawLimits.getBlockedGuarantee();
        Assertions.assertEquals(1, blockedGuarantee.size());
        Assertions.assertEquals(currency.name(), blockedGuarantee.get(0).getCurrency().getCurrencyCode());
        for (final Money money : blockedGuarantee) {
            AssertUtils.assertEquals(0, money.getValue());
        }
    }

    @Test
    void getWithdrawLimits_returnsCurrencyWithBalance_whenBalanceInitialized() {
        final String accountId = "2000124699";

        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000.0);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                accountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final WithdrawLimits withdrawLimits = service.getWithdrawLimits(accountId);

        final BigDecimal balance = DataStructsHelper.getBalance(withdrawLimits.getMoney(), currency);
        final BigDecimal blockedBalance = DataStructsHelper.getBalance(withdrawLimits.getBlocked(), currency);
        final BigDecimal blockedGuaranteeBalance = DataStructsHelper.getBalance(withdrawLimits.getBlockedGuarantee(), currency);

        AssertUtils.assertEquals(balanceConfig.getInitialBalance(), balance);
        AssertUtils.assertEquals(0, blockedBalance);
        AssertUtils.assertEquals(0, blockedGuaranteeBalance);
    }

    @Test
    void getWithdrawLimits_returnsCurrencyWithBalance_afterAddInvestment() {
        final String accountId = "2000124699";

        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BigDecimal initialInvestment = BigDecimal.valueOf(1000);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                accountId,
                dateTime,
                Currency.USD,
                0.003,
                TestData.createBalanceConfig(0.0)
        );
        service.addInvestment(accountId, dateTime, currency, initialInvestment);

        final WithdrawLimits withdrawLimits = service.getWithdrawLimits(accountId);

        final BigDecimal balance = DataStructsHelper.getBalance(withdrawLimits.getMoney(), currency);
        final BigDecimal blockedBalance = DataStructsHelper.getBalance(withdrawLimits.getBlocked(), currency);
        final BigDecimal blockedGuaranteeBalance = DataStructsHelper.getBalance(withdrawLimits.getBlockedGuarantee(), currency);

        AssertUtils.assertEquals(initialInvestment, balance);
        AssertUtils.assertEquals(0, blockedBalance);
        AssertUtils.assertEquals(0, blockedGuaranteeBalance);
    }

    // endregion

    private void placeMarketOrder(
            final String accountId,
            final String ticker,
            final int lotsCount,
            final OperationType operationType,
            final double price
    ) throws IOException {
        final Candle candle = new Candle().setClosePrice(DecimalUtils.setDefaultScale(price));
        Mockito.when(marketService.getLastCandle(ticker, service.getCurrentDateTime())).thenReturn(candle);

        final MarketOrderRequest orderRequest = new MarketOrderRequest((long) lotsCount, operationType);
        service.placeMarketOrder(accountId, ticker, orderRequest);
    }

    @Test
    void getCurrentPrice() throws IOException {
        final String ticker = "ticker";
        final BigDecimal price = BigDecimal.valueOf(100);
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 18, 59, 59);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                null,
                dateTime,
                Currency.USD,
                0.003,
                TestData.createBalanceConfig(0.0)
        );

        Mockito.when(marketService.getLastCandle(ticker, service.getCurrentDateTime()))
                .thenReturn(new Candle().setClosePrice(price));

        final BigDecimal currentPrice = service.getCurrentPrice(ticker);

        AssertUtils.assertEquals(price, currentPrice);
    }

    private void mockShare(String ticker, Currency currency, int lotSize) {
        final Share share = TestData.createShare(ticker, currency, lotSize);
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);
    }

}