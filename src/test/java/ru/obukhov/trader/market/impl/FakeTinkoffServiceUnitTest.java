package ru.obukhov.trader.market.impl;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import ru.obukhov.trader.market.model.CurrencyPosition;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.market.model.MarketOrderRequest;
import ru.obukhov.trader.market.model.Operation;
import ru.obukhov.trader.market.model.OperationType;
import ru.obukhov.trader.market.model.OperationTypeWithCommission;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.web.model.BalanceConfig;

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
    private RealTinkoffService realTinkoffService;
    @InjectMocks
    private TinkoffServices tinkoffServices;

    private FakeTinkoffService service;

    // region init tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forInit_movesCurrentDateTimeToCeilingWorkTime() {
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
    @MethodSource("getData_forInit_movesCurrentDateTimeToCeilingWorkTime")
    void constructor_movesCurrentDateTimeToCeilingWorkTime(
            @Nullable final String brokerAccountId,
            final OffsetDateTime dateTime,
            final OffsetDateTime expectedCurrentDateTime
    ) {
        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
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

    private void constructor_initsBalance(final String brokerAccountId, final BalanceConfig balanceConfig, final double expectedBalance) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 1);
        final Currency currency = Currency.RUB;

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final BigDecimal balance = service.getCurrentBalance(brokerAccountId, currency);

        AssertUtils.assertEquals(expectedBalance, balance);
    }

    // endregion

    // region nextMinute tests

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void nextMinute_movesToNextMinute_whenMiddleOfWorkDay(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
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

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void nextMinute_movesToStartOfNextDay_whenAtEndOfWorkDay(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 18, 59, 59);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
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

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void nextMinute_movesToStartOfNextWeek_whenEndOfWorkWeek(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 9, 18, 59, 59);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
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

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getOperations_filtersOperationsByInterval(@Nullable final String brokerAccountId) throws IOException {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
                dateTime,
                Currency.RUB,
                0.003,
                balanceConfig
        );

        final String ticker = "ticker";
        final int lotSize = 10;

        final MarketInstrument instrument = TestData.createMarketInstrument(ticker, lotSize);
        Mockito.when(realTinkoffService.searchMarketInstrument(ticker)).thenReturn(instrument);

        final OffsetDateTime operation1DateTime = service.getCurrentDateTime();
        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.BUY, 100);
        final OffsetDateTime operation2DateTime = service.nextMinute();
        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.BUY, 200);
        final OffsetDateTime operation3DateTime = service.nextMinute();
        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.SELL, 300);

        final Interval wholeInterval = Interval.of(dateTime, dateTime.plusMinutes(2));
        final List<Operation> allOperations = service.getOperations(brokerAccountId, wholeInterval, ticker);

        final Operation expectedOperation1 = TestData.createOperation(
                operation1DateTime,
                OperationTypeWithCommission.BUY,
                100,
                10,
                3
        );
        final Operation expectedOperation2 = TestData.createOperation(
                operation2DateTime,
                OperationTypeWithCommission.BUY,
                200,
                10,
                6
        );
        final Operation expectedOperation3 = TestData.createOperation(
                operation3DateTime,
                OperationTypeWithCommission.SELL,
                300,
                10,
                9
        );

        AssertUtils.assertListsAreEqual(List.of(expectedOperation1, expectedOperation2, expectedOperation3), allOperations);

        final Interval localInterval = Interval.of(dateTime.plusMinutes(1), dateTime.plusMinutes(1));
        final List<Operation> localOperations = service.getOperations(brokerAccountId, localInterval, ticker);
        AssertUtils.assertListsAreEqual(List.of(expectedOperation2), localOperations);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getOperations_filtersOperationsByTicker_whenTickerIsNotNull(@Nullable final String brokerAccountId) throws IOException {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
                dateTime,
                Currency.RUB,
                0.003,
                balanceConfig
        );

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final int lotSize = 10;

        final MarketInstrument instrument1 = TestData.createMarketInstrument(ticker1, lotSize);
        Mockito.when(realTinkoffService.searchMarketInstrument(ticker1)).thenReturn(instrument1);
        final MarketInstrument instrument2 = TestData.createMarketInstrument(ticker2, lotSize);
        Mockito.when(realTinkoffService.searchMarketInstrument(ticker2)).thenReturn(instrument2);

        final OffsetDateTime ticker1OperationDateTime = service.getCurrentDateTime();
        placeMarketOrder(brokerAccountId, ticker1, 1, OperationType.BUY, 100);
        final OffsetDateTime ticker2Operation1DateTime = service.nextMinute();
        placeMarketOrder(brokerAccountId, ticker2, 1, OperationType.BUY, 200);
        final OffsetDateTime ticker2Operation2DateTime = service.nextMinute();
        placeMarketOrder(brokerAccountId, ticker2, 1, OperationType.SELL, 300);

        final Interval interval = Interval.of(dateTime, dateTime.plusMinutes(2));
        final List<Operation> ticker1Operations = service.getOperations(brokerAccountId, interval, ticker1);
        final List<Operation> ticker2Operations = service.getOperations(brokerAccountId, interval, ticker2);

        final Operation expectedTicker1Operation = TestData.createOperation(
                ticker1OperationDateTime,
                OperationTypeWithCommission.BUY,
                100,
                10,
                3
        );
        AssertUtils.assertListsAreEqual(List.of(expectedTicker1Operation), ticker1Operations);
        final Operation expectedTicker2Operation1 = TestData.createOperation(
                ticker2Operation1DateTime,
                OperationTypeWithCommission.BUY,
                200,
                10,
                6
        );
        final Operation expectedTicker2Operation2 = TestData.createOperation(
                ticker2Operation2DateTime,
                OperationTypeWithCommission.SELL,
                300,
                10,
                9
        );
        AssertUtils.assertListsAreEqual(List.of(expectedTicker2Operation1, expectedTicker2Operation2), ticker2Operations);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getOperations_doesNotFilterOperationsByTicker_whenTickerIsNull(@Nullable final String brokerAccountId) throws IOException {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
                dateTime,
                Currency.RUB,
                0.003,
                balanceConfig
        );

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final int lotSize = 10;

        final MarketInstrument instrument1 = TestData.createMarketInstrument(ticker1, lotSize);
        Mockito.when(realTinkoffService.searchMarketInstrument(ticker1)).thenReturn(instrument1);
        final MarketInstrument instrument2 = TestData.createMarketInstrument(ticker2, lotSize);
        Mockito.when(realTinkoffService.searchMarketInstrument(ticker2)).thenReturn(instrument2);

        final OffsetDateTime operation1DateTime = service.getCurrentDateTime();
        placeMarketOrder(brokerAccountId, ticker1, 1, OperationType.BUY, 100);
        final OffsetDateTime operation2DateTime = service.nextMinute();
        placeMarketOrder(brokerAccountId, ticker2, 1, OperationType.BUY, 200);
        final OffsetDateTime operation3DateTime = service.nextMinute();
        placeMarketOrder(brokerAccountId, ticker2, 1, OperationType.SELL, 300);

        final Interval interval = Interval.of(dateTime, dateTime.plusMinutes(2));
        final List<Operation> operations = service.getOperations(brokerAccountId, interval, null);

        final Operation expectedOperation1 = TestData.createOperation(
                operation1DateTime,
                OperationTypeWithCommission.BUY,
                100,
                10,
                3
        );
        final Operation expectedOperation2 = TestData.createOperation(
                operation2DateTime,
                OperationTypeWithCommission.BUY,
                200,
                10,
                6
        );
        final Operation expectedOperation3 = TestData.createOperation(
                operation3DateTime,
                OperationTypeWithCommission.SELL,
                300,
                10,
                9
        );
        AssertUtils.assertListsAreEqual(List.of(expectedOperation1, expectedOperation2, expectedOperation3), operations);
    }

    // endregion

    // region placeMarketOrder tests. Implicit tests for getPortfolioPositions

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void placeMarketOrder_buy_throwsIllegalArgumentException_whenNotEnoughBalance(@Nullable final String brokerAccountId) throws IOException {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000.0);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final String ticker = "ticker";

        final MarketInstrument instrument = TestData.createMarketInstrument(ticker, 10);
        Mockito.when(realTinkoffService.searchMarketInstrument(ticker)).thenReturn(instrument);

        final Executable executable = () -> placeMarketOrder(brokerAccountId, ticker, 2, OperationType.BUY, 500);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "balance can't be negative");

        Assertions.assertTrue(service.getPortfolioPositions(brokerAccountId).isEmpty());
        AssertUtils.assertEquals(1000, service.getCurrentBalance(brokerAccountId, currency));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void placeMarketOrder_buy_createsNewPosition_whenNoPositions(@Nullable final String brokerAccountId) throws IOException {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final String ticker = "ticker";

        final MarketInstrument instrument = TestData.createMarketInstrument(ticker, 10);
        Mockito.when(realTinkoffService.searchMarketInstrument(ticker)).thenReturn(instrument);

        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.BUY, 1000);

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
    void placeMarketOrder_buy_addsValueToExistingPosition_whenPositionAlreadyExists(@Nullable final String brokerAccountId) throws IOException {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final String ticker = "ticker";

        final MarketInstrument instrument = TestData.createMarketInstrument(ticker, 10);
        Mockito.when(realTinkoffService.searchMarketInstrument(ticker)).thenReturn(instrument);

        placeMarketOrder(brokerAccountId, ticker, 2, OperationType.BUY, 1000);
        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.BUY, 4000);

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions(brokerAccountId);
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.getTicker());
        Assertions.assertEquals(30, portfolioPosition.getCount());
        AssertUtils.assertEquals(2000, portfolioPosition.getAveragePositionPrice().value());
        AssertUtils.assertEquals(939820, service.getCurrentBalance(brokerAccountId, currency));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void placeMarketOrder_buy_createsMultiplePositions_whenDifferentTickersAreBought(@Nullable final String brokerAccountId) throws IOException {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final String ticker3 = "ticker3";

        final MarketInstrument instrument1 = TestData.createMarketInstrument(ticker1, 10);
        Mockito.when(realTinkoffService.searchMarketInstrument(ticker1)).thenReturn(instrument1);
        final MarketInstrument instrument2 = TestData.createMarketInstrument(ticker2, 2);
        Mockito.when(realTinkoffService.searchMarketInstrument(ticker2)).thenReturn(instrument2);
        final MarketInstrument instrument3 = TestData.createMarketInstrument(ticker3, 1);
        Mockito.when(realTinkoffService.searchMarketInstrument(ticker3)).thenReturn(instrument3);

        placeMarketOrder(brokerAccountId, ticker1, 1, OperationType.BUY, 1000);
        placeMarketOrder(brokerAccountId, ticker2, 3, OperationType.BUY, 100);
        placeMarketOrder(brokerAccountId, ticker3, 1, OperationType.BUY, 500);

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
    void placeMarketOrder_sell_throwsIllegalArgumentException_whenSellsMoreLotsThanExists(@Nullable final String brokerAccountId) throws IOException {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);
        final int lotSize = 10;

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final String ticker = "ticker";

        final MarketInstrument instrument = TestData.createMarketInstrument(ticker, lotSize);
        Mockito.when(realTinkoffService.searchMarketInstrument(ticker)).thenReturn(instrument);

        placeMarketOrder(brokerAccountId, ticker, 2, OperationType.BUY, 1000);
        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.BUY, 4000);
        final Executable sellExecutable = () -> placeMarketOrder(brokerAccountId, ticker, 4, OperationType.SELL, 3000);
        final String expectedMessage = "lotsCount 4 can't be greater than existing position lots count 3";
        Assertions.assertThrows(IllegalArgumentException.class, sellExecutable, expectedMessage);

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions(brokerAccountId);
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.getTicker());
        Assertions.assertEquals(30, portfolioPosition.getCount());
        AssertUtils.assertEquals(2000, portfolioPosition.getAveragePositionPrice().value());
        AssertUtils.assertEquals(939820, service.getCurrentBalance(brokerAccountId, currency));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void placeMarketOrder_sell_removesPosition_whenAllLotsAreSold(@Nullable final String brokerAccountId) throws IOException {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);
        final int lotSize = 10;

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final String ticker = "ticker";

        final MarketInstrument instrument = TestData.createMarketInstrument(ticker, lotSize);
        Mockito.when(realTinkoffService.searchMarketInstrument(ticker)).thenReturn(instrument);

        placeMarketOrder(brokerAccountId, ticker, 2, OperationType.BUY, 1000);
        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.BUY, 4000);
        placeMarketOrder(brokerAccountId, ticker, 3, OperationType.SELL, 3000);

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions(brokerAccountId);
        Assertions.assertTrue(positions.isEmpty());
        AssertUtils.assertEquals(1029550, service.getCurrentBalance(brokerAccountId, currency));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void placeMarketOrder_sell_reducesLotsCount(@Nullable final String brokerAccountId) throws IOException {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);
        final int lotSize = 10;

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final String ticker = "ticker";

        final MarketInstrument instrument = TestData.createMarketInstrument(ticker, lotSize);
        Mockito.when(realTinkoffService.searchMarketInstrument(ticker)).thenReturn(instrument);

        placeMarketOrder(brokerAccountId, ticker, 2, OperationType.BUY, 1000);
        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.BUY, 4000);
        placeMarketOrder(brokerAccountId, ticker, 1, OperationType.SELL, 3000);

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions(brokerAccountId);
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.getTicker());
        Assertions.assertEquals(20, portfolioPosition.getCount());
        AssertUtils.assertEquals(2000, portfolioPosition.getAveragePositionPrice().value());
        AssertUtils.assertEquals(969730, service.getCurrentBalance(brokerAccountId, currency));
    }

    // endregion

    // region getPortfolioCurrencies tests

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getPortfolioCurrencies_returnsAllCurrencies_whenCurrenciesAreNotInitialized(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
                dateTime,
                Currency.USD,
                0.003,
                TestData.createBalanceConfig(0.0)
        );

        final List<CurrencyPosition> currencies = service.getPortfolioCurrencies(brokerAccountId);

        Assertions.assertEquals(Currency.values().length, currencies.size());
        for (final Currency currency : Currency.values()) {
            if (currencies.stream().noneMatch(portfolioCurrency -> portfolioCurrency.currency() == currency)) {
                Assertions.fail("Currency " + currency + " found in getPortfolioCurrencies result");
            }
        }

        for (final CurrencyPosition portfolioCurrency : currencies) {
            AssertUtils.assertEquals(0, portfolioCurrency.balance());
            Assertions.assertNull(portfolioCurrency.blocked());
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getPortfolioCurrencies_returnsCurrencyWithBalance_whenBalanceInitialized(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000.0);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
                dateTime,
                currency,
                0.003,
                balanceConfig
        );

        final List<CurrencyPosition> currencies = service.getPortfolioCurrencies(brokerAccountId);

        CurrencyPosition portfolioCurrency = currencies.stream()
                .filter(currentPortfolioCurrency -> currentPortfolioCurrency.currency() == currency)
                .findFirst()
                .orElseThrow();

        AssertUtils.assertEquals(balanceConfig.getInitialBalance(), portfolioCurrency.balance());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getPortfolioCurrencies_returnsCurrencyWithBalance_afterAddInvestment(@Nullable final String brokerAccountId) {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(1000);

        service = new FakeTinkoffService(
                MARKET_PROPERTIES,
                tinkoffServices,
                brokerAccountId,
                dateTime,
                Currency.USD,
                0.003,
                TestData.createBalanceConfig(0.0)
        );
        service.addInvestment(brokerAccountId, dateTime, currency, balance);

        final List<CurrencyPosition> currencies = service.getPortfolioCurrencies(brokerAccountId);

        CurrencyPosition portfolioCurrency = currencies.stream()
                .filter(currentPortfolioCurrency -> currentPortfolioCurrency.currency() == currency)
                .findFirst()
                .orElseThrow();

        AssertUtils.assertEquals(balance, portfolioCurrency.balance());
    }

    // endregion

    private void placeMarketOrder(
            @Nullable final String brokerAccountId,
            final String ticker,
            final int lots,
            final OperationType operationType,
            final double price
    ) throws IOException {
        final Candle candle = new Candle().setClosePrice(DecimalUtils.setDefaultScale(price));
        Mockito.when(marketService.getLastCandle(ticker, service.getCurrentDateTime())).thenReturn(candle);

        final MarketOrderRequest orderRequest = new MarketOrderRequest(lots, operationType);
        service.placeMarketOrder(brokerAccountId, ticker, orderRequest);
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

}