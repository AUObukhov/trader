package ru.obukhov.trader.market.impl;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;
import ru.tinkoff.invest.openapi.model.rest.MarketOrderRequest;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class FakeTinkoffServiceUnitTest {

    private static final MarketProperties MARKET_PROPERTIES = TestData.createMarketProperties();

    @Mock
    private MarketService marketService;
    @Mock
    private RealTinkoffService realTinkoffService;

    private FakeTinkoffService service;

    @BeforeEach
    void setUpEach() {
        this.service = new FakeTinkoffService(MARKET_PROPERTIES, 0.003, marketService, realTinkoffService);
    }

    // region init tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forInit() {
        return Stream.of(
                Arguments.of(
                        null,
                        DateTimeTestData.createDateTime(2020, 10, 5, 12),
                        DateTimeTestData.createDateTime(2020, 10, 5, 12)
                ),
                Arguments.of(
                        "2000124699",
                        DateTimeTestData.createDateTime(2020, 10, 5, 12),
                        DateTimeTestData.createDateTime(2020, 10, 5, 12)
                ),

                Arguments.of(
                        null,
                        DateTimeTestData.createDateTime(2020, 10, 5, 19),
                        DateTimeTestData.createDateTime(2020, 10, 6, 10)
                ),
                Arguments.of(
                        "2000124699",
                        DateTimeTestData.createDateTime(2020, 10, 5, 19),
                        DateTimeTestData.createDateTime(2020, 10, 6, 10)
                ),

                Arguments.of(
                        null,
                        DateTimeTestData.createDateTime(2020, 10, 5, 19, 20),
                        DateTimeTestData.createDateTime(2020, 10, 6, 10)
                ),
                Arguments.of(
                        "2000124699",
                        DateTimeTestData.createDateTime(2020, 10, 5, 19, 20),
                        DateTimeTestData.createDateTime(2020, 10, 6, 10)
                ),

                Arguments.of(
                        null,
                        DateTimeTestData.createDateTime(2020, 10, 9, 19), // friday
                        DateTimeTestData.createDateTime(2020, 10, 12, 10) // monday
                ),
                Arguments.of(
                        "2000124699",
                        DateTimeTestData.createDateTime(2020, 10, 9, 19), // friday
                        DateTimeTestData.createDateTime(2020, 10, 12, 10) // monday
                ),

                Arguments.of(
                        null,
                        DateTimeTestData.createDateTime(2020, 10, 10, 12), // saturday
                        DateTimeTestData.createDateTime(2020, 10, 12, 10) // monday
                ),
                Arguments.of(
                        "2000124699",
                        DateTimeTestData.createDateTime(2020, 10, 10, 12), // saturday
                        DateTimeTestData.createDateTime(2020, 10, 12, 10) // monday
                ),

                Arguments.of(
                        null,
                        DateTimeTestData.createDateTime(2020, 10, 9, 9),
                        DateTimeTestData.createDateTime(2020, 10, 9, 10)
                ),
                Arguments.of(
                        "2000124699",
                        DateTimeTestData.createDateTime(2020, 10, 9, 9),
                        DateTimeTestData.createDateTime(2020, 10, 9, 10)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forInit")
    void init(@Nullable final String brokerAccountId, final OffsetDateTime dateTime, final OffsetDateTime expectedCurrentDateTime) {
        service.init(brokerAccountId, dateTime);

        Assertions.assertEquals(expectedCurrentDateTime, service.getCurrentDateTime());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void init_throwsIllegalArgumentException_whenCurrencyIsNullAndBalanceIsNotNull(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(10);

        final Executable executable = () -> service.init(brokerAccountId, dateTime, null, balanceConfig);
        final String expectedMessage = "currency and balance must be both null or both not null";
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void init_throwsIllegalArgumentException_whenCurrencyIsNotNullAndInitialBalanceIsNull(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);

        final Executable executable = () -> service.init(brokerAccountId, dateTime, Currency.RUB, new BalanceConfig());
        final String expectedMessage = "currency and balanceConfig.balance must be both null or both not null";
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void init_clearsPortfolio(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000);

        service.init(brokerAccountId, dateTime, currency, balanceConfig);
        final String ticker = "ticker";
        final int lotSize = 10;

        Mocker.createAndMockInstrument(realTinkoffService, ticker, lotSize);

        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.BUY, BigDecimal.valueOf(100));
        service.nextMinute();
        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.BUY, BigDecimal.valueOf(200));
        service.nextMinute();
        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.SELL, BigDecimal.valueOf(300));

        final OffsetDateTime newDateTime = DateTimeTestData.createDateTime(2021, 10, 5, 12);
        final BalanceConfig newBalanceConfig = TestData.createBalanceConfig(1000);

        service.init(brokerAccountId, newDateTime, currency, newBalanceConfig);

        Assertions.assertEquals(newDateTime, service.getCurrentDateTime());
        AssertUtils.assertEquals(newBalanceConfig.getInitialBalance(), service.getCurrentBalance(brokerAccountId, currency));

        final Interval interval = Interval.of(dateTime, newDateTime);
        Assertions.assertTrue(service.getOperations(brokerAccountId, interval, ticker).isEmpty());

        final SortedMap<OffsetDateTime, BigDecimal> investments = service.getInvestments(currency);
        Assertions.assertEquals(1, investments.size());
        AssertUtils.assertEquals(newBalanceConfig.getInitialBalance(), investments.get(newDateTime));

        Assertions.assertTrue(service.getPortfolioPositions(brokerAccountId).isEmpty());
    }

    // endregion

    // region nextMinute tests

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void nextMinute_movesToNextMinute_whenMiddleOfWorkDay(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);

        service.init(brokerAccountId, dateTime);

        final OffsetDateTime nextMinuteDateTime = service.nextMinute();

        final OffsetDateTime expected = dateTime.plusMinutes(1);
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, service.getCurrentDateTime());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void nextMinute_movesToStartOfNextDay_whenAtEndOfWorkDay(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 18, 59, 59);

        service.init(brokerAccountId, dateTime);

        final OffsetDateTime nextMinuteDateTime = service.nextMinute();

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), MARKET_PROPERTIES.getWorkStartTime());
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, service.getCurrentDateTime());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void nextMinute_movesToStartOfNextWeek_whenEndOfWorkWeek(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 9, 18, 59, 59);

        service.init(brokerAccountId, dateTime);

        final OffsetDateTime nextMinuteDateTime = service.nextMinute();

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(3), MARKET_PROPERTIES.getWorkStartTime());
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, service.getCurrentDateTime());
    }

    // endregion

    // region getOperations tests

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getOperations_filtersOperationsByInterval(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000);

        service.init(brokerAccountId, dateTime, Currency.RUB, balanceConfig);

        final String ticker = "ticker";
        final int lotSize = 10;

        Mocker.createAndMockInstrument(realTinkoffService, ticker, lotSize);

        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.BUY, BigDecimal.valueOf(100));
        service.nextMinute();
        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.BUY, BigDecimal.valueOf(200));
        service.nextMinute();
        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.SELL, BigDecimal.valueOf(300));

        final Interval wholeInterval = Interval.of(dateTime, dateTime.plusMinutes(2));
        final List<Operation> allOperations = service.getOperations(brokerAccountId, wholeInterval, ticker);

        Assertions.assertEquals(3, allOperations.size());

        final Interval localInterval = Interval.of(dateTime.plusMinutes(1), dateTime.plusMinutes(1));
        final List<Operation> localOperations = service.getOperations(brokerAccountId, localInterval, ticker);
        Assertions.assertEquals(1, localOperations.size());
        Assertions.assertEquals(dateTime.plusMinutes(1), localOperations.get(0).getDate());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getOperations_filtersOperationsByTicker_whenTickerIsNotNull(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000);

        service.init(brokerAccountId, dateTime, Currency.RUB, balanceConfig);

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final int lotSize = 10;

        Mocker.createAndMockInstrument(realTinkoffService, ticker1, lotSize);
        Mocker.createAndMockInstrument(realTinkoffService, ticker2, lotSize);

        placeMarketOrder(brokerAccountId, ticker1, 1, OperationType.BUY, BigDecimal.valueOf(100));
        service.nextMinute();
        placeMarketOrder(brokerAccountId, ticker2, 1, OperationType.BUY, BigDecimal.valueOf(200));
        service.nextMinute();
        placeMarketOrder(brokerAccountId, ticker2, 1, OperationType.SELL, BigDecimal.valueOf(300));

        final Interval interval = Interval.of(dateTime, dateTime.plusMinutes(2));
        final List<Operation> ticker1Operations = service.getOperations(brokerAccountId, interval, ticker1);
        Assertions.assertEquals(1, ticker1Operations.size());
        Assertions.assertEquals(dateTime, ticker1Operations.get(0).getDate());

        final List<Operation> ticker2Operations = service.getOperations(brokerAccountId, interval, ticker2);
        Assertions.assertEquals(2, ticker2Operations.size());
        Assertions.assertEquals(dateTime.plusMinutes(1), ticker2Operations.get(0).getDate());
        Assertions.assertEquals(dateTime.plusMinutes(2), ticker2Operations.get(1).getDate());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getOperations_doesNotFilterOperationsByTicker_whenTickerIsNull(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000);

        service.init(brokerAccountId, dateTime, Currency.RUB, balanceConfig);

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final int lotSize = 10;

        Mocker.createAndMockInstrument(realTinkoffService, ticker1, lotSize);
        Mocker.createAndMockInstrument(realTinkoffService, ticker2, lotSize);

        placeMarketOrder(brokerAccountId, ticker1, 1, OperationType.BUY, BigDecimal.valueOf(100));
        service.nextMinute();
        placeMarketOrder(brokerAccountId, ticker2, 1, OperationType.BUY, BigDecimal.valueOf(200));
        service.nextMinute();
        placeMarketOrder(brokerAccountId, ticker2, 1, OperationType.SELL, BigDecimal.valueOf(300));

        final Interval interval = Interval.of(dateTime, dateTime.plusMinutes(2));
        final List<Operation> operations = service.getOperations(brokerAccountId, interval, null);

        Assertions.assertEquals(3, operations.size());
    }

    // endregion

    // region placeMarketOrder tests. Implicit tests for getPortfolioPositions

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void placeMarketOrder_buy_throwsIllegalArgumentException_whenNotEnoughBalance(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000);

        service.init(brokerAccountId, dateTime, currency, balanceConfig);

        final String ticker = "ticker";

        Mocker.createAndMockInstrument(realTinkoffService, ticker, 10);

        final Executable executable = () -> placeMarketOrder(brokerAccountId, ticker, 2, OperationType.BUY, BigDecimal.valueOf(500));
        Assertions.assertThrows(IllegalArgumentException.class, executable, "balance can't be negative");

        Assertions.assertTrue(service.getPortfolioPositions(brokerAccountId).isEmpty());
        AssertUtils.assertEquals(1000, service.getCurrentBalance(brokerAccountId, currency));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void placeMarketOrder_buy_createsNewPosition_whenNoPositions(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000);

        service.init(brokerAccountId, dateTime, currency, balanceConfig);

        final String ticker = "ticker";

        Mocker.createAndMockInstrument(realTinkoffService, ticker, 10);

        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.BUY, BigDecimal.valueOf(1000));

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions(brokerAccountId);
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.getTicker());
        Assertions.assertEquals(10, portfolioPosition.getCount());
        AssertUtils.assertEquals(989970, service.getCurrentBalance(brokerAccountId, currency));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void placeMarketOrder_buy_addsValueToExistingPosition_whenPositionAlreadyExists(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000);

        service.init(brokerAccountId, dateTime, currency, balanceConfig);

        final String ticker = "ticker";

        Mocker.createAndMockInstrument(realTinkoffService, ticker, 10);

        placeMarketOrder(brokerAccountId, ticker, 2, OperationType.BUY, BigDecimal.valueOf(1000));
        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.BUY, BigDecimal.valueOf(4000));

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions(brokerAccountId);
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.getTicker());
        Assertions.assertEquals(30, portfolioPosition.getCount());
        AssertUtils.assertEquals(2000, portfolioPosition.getAveragePositionPrice());
        AssertUtils.assertEquals(939820, service.getCurrentBalance(brokerAccountId, currency));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void placeMarketOrder_buy_createsMultiplePositions_whenDifferentTickersAreBought(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000);

        service.init(brokerAccountId, dateTime, currency, balanceConfig);

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final String ticker3 = "ticker3";

        Mocker.createAndMockInstrument(realTinkoffService, ticker1, 10);
        Mocker.createAndMockInstrument(realTinkoffService, ticker2, 2);
        Mocker.createAndMockInstrument(realTinkoffService, ticker3, 1);

        placeMarketOrder(brokerAccountId, ticker1, 1, OperationType.BUY, BigDecimal.valueOf(1000));
        placeMarketOrder(brokerAccountId, ticker2, 3, OperationType.BUY, BigDecimal.valueOf(100));
        placeMarketOrder(brokerAccountId, ticker3, 1, OperationType.BUY, BigDecimal.valueOf(500));

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions(brokerAccountId);
        Assertions.assertEquals(3, positions.size());

        final Iterator<PortfolioPosition> positionIterator = positions.iterator();
        final PortfolioPosition portfolioPosition1 = positionIterator.next();
        Assertions.assertEquals(ticker1, portfolioPosition1.getTicker());
        Assertions.assertEquals(10, portfolioPosition1.getCount());

        final PortfolioPosition portfolioPosition2 = positionIterator.next();
        Assertions.assertEquals(ticker2, portfolioPosition2.getTicker());
        Assertions.assertEquals(6, portfolioPosition2.getCount());

        final PortfolioPosition portfolioPosition3 = positionIterator.next();
        Assertions.assertEquals(ticker3, portfolioPosition3.getTicker());
        Assertions.assertEquals(1, portfolioPosition3.getCount());

        AssertUtils.assertEquals(988866.7, service.getCurrentBalance(brokerAccountId, currency));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void placeMarketOrder_sell_throwsIllegalArgumentException_whenSellsMoreLotsThanExists(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000);
        final int lotSize = 10;

        service.init(brokerAccountId, dateTime, currency, balanceConfig);

        final String ticker = "ticker";

        Mocker.createAndMockInstrument(realTinkoffService, ticker, lotSize);

        placeMarketOrder(brokerAccountId, ticker, 2, OperationType.BUY, BigDecimal.valueOf(1000));
        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.BUY, BigDecimal.valueOf(4000));
        final Executable sellExecutable = () -> placeMarketOrder(brokerAccountId, ticker, 4, OperationType.SELL, BigDecimal.valueOf(3000));
        final String expectedMessage = "lotsCount 4 can't be greater than existing position lots count 3";
        Assertions.assertThrows(IllegalArgumentException.class, sellExecutable, expectedMessage);

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions(brokerAccountId);
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.getTicker());
        Assertions.assertEquals(30, portfolioPosition.getCount());
        AssertUtils.assertEquals(2000, portfolioPosition.getAveragePositionPrice());
        AssertUtils.assertEquals(939820, service.getCurrentBalance(brokerAccountId, currency));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void placeMarketOrder_sell_removesPosition_whenAllLotsAreSold(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000);
        final int lotSize = 10;

        service.init(brokerAccountId, dateTime, currency, balanceConfig);

        final String ticker = "ticker";

        Mocker.createAndMockInstrument(realTinkoffService, ticker, lotSize);

        placeMarketOrder(brokerAccountId, ticker, 2, OperationType.BUY, BigDecimal.valueOf(1000));
        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.BUY, BigDecimal.valueOf(4000));
        placeMarketOrder(brokerAccountId, ticker, 3, OperationType.SELL, BigDecimal.valueOf(3000));

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions(brokerAccountId);
        Assertions.assertTrue(positions.isEmpty());
        AssertUtils.assertEquals(1029550, service.getCurrentBalance(brokerAccountId, currency));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void placeMarketOrder_sell_reducesLotsCount(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000);
        final int lotSize = 10;

        service.init(brokerAccountId, dateTime, currency, balanceConfig);

        final String ticker = "ticker";

        Mocker.createAndMockInstrument(realTinkoffService, ticker, lotSize);

        placeMarketOrder(brokerAccountId, ticker, 2, OperationType.BUY, BigDecimal.valueOf(1000));
        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.BUY, BigDecimal.valueOf(4000));
        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.SELL, BigDecimal.valueOf(3000));

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions(brokerAccountId);
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.getTicker());
        Assertions.assertEquals(20, portfolioPosition.getCount());
        AssertUtils.assertEquals(2000, portfolioPosition.getAveragePositionPrice());
        AssertUtils.assertEquals(969730, service.getCurrentBalance(brokerAccountId, currency));
    }

    // endregion

    // region getPortfolioCurrencies tests

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getPortfolioCurrencies_returnsAllCurrencies_whenCurrenciesAreNotInitialized(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);

        service.init(brokerAccountId, dateTime);

        final List<CurrencyPosition> currencies = service.getPortfolioCurrencies(brokerAccountId);

        Assertions.assertEquals(Currency.values().length, currencies.size());
        for (final Currency currency : Currency.values()) {
            if (currencies.stream().noneMatch(portfolioCurrency -> portfolioCurrency.getCurrency() == currency)) {
                Assertions.fail("Currency " + currency + " found in getPortfolioCurrencies result");
            }
        }

        for (final CurrencyPosition portfolioCurrency : currencies) {
            AssertUtils.assertEquals(0, portfolioCurrency.getBalance());
            Assertions.assertNull(portfolioCurrency.getBlocked());
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getPortfolioCurrencies_returnsCurrencyWithBalance_whenBalanceInitialized(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000);

        service.init(brokerAccountId, dateTime, currency, balanceConfig);

        final List<CurrencyPosition> currencies = service.getPortfolioCurrencies(brokerAccountId);

        CurrencyPosition portfolioCurrency = currencies.stream()
                .filter(currentPortfolioCurrency -> currentPortfolioCurrency.getCurrency() == currency)
                .findFirst()
                .orElseThrow();

        AssertUtils.assertEquals(balanceConfig.getInitialBalance(), portfolioCurrency.getBalance());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getPortfolioCurrencies_returnsCurrencyWithBalance_afterIncrementBalance(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(1000);

        service.init(brokerAccountId, dateTime);
        service.incrementBalance(brokerAccountId, currency, balance);

        final List<CurrencyPosition> currencies = service.getPortfolioCurrencies(brokerAccountId);

        CurrencyPosition portfolioCurrency = currencies.stream()
                .filter(currentPortfolioCurrency -> currentPortfolioCurrency.getCurrency() == currency)
                .findFirst()
                .orElseThrow();

        AssertUtils.assertEquals(balance, portfolioCurrency.getBalance());
    }

    // endregion

    private void placeMarketOrder(
            @Nullable final String brokerAccountId,
            final String ticker,
            final int lots,
            final OperationType operationType,
            final BigDecimal price
    ) {
        final Candle candle = new Candle().setOpenPrice(price);
        Mockito.when(marketService.getLastCandle(ticker, service.getCurrentDateTime())).thenReturn(candle);

        MarketOrderRequest orderRequest = new MarketOrderRequest()
                .lots(lots)
                .operation(operationType);
        service.placeMarketOrder(brokerAccountId, ticker, orderRequest);
    }

}