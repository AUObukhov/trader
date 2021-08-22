package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestData;
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

    private static final TradingProperties TRADING_PROPERTIES = TestData.createTradingProperties();

    @Mock
    private MarketService marketService;
    @Mock
    private RealTinkoffService realTinkoffService;

    private FakeTinkoffService service;

    @BeforeEach
    void setUpEach() {
        this.service = new FakeTinkoffService(TRADING_PROPERTIES, marketService, realTinkoffService);
    }

    // region init tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forInit() {
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
    @MethodSource("getData_forInit")
    void init(final OffsetDateTime dateTime, final OffsetDateTime expectedCurrentDateTime) {
        service.init(dateTime);

        Assertions.assertEquals(expectedCurrentDateTime, service.getCurrentDateTime());
    }

    @Test
    void init_throwsIllegalArgumentException_whenCurrencyIsNullAndBalanceIsNotNull() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);

        final Executable executable = () -> service.init(dateTime, null, BigDecimal.TEN);
        final String expectedMessage = "currency and balance must be both null or both not null";
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void init_throwsIllegalArgumentException_whenCurrencyIsNotNullAndBalanceIsNull() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);

        final Executable executable = () -> service.init(dateTime, Currency.RUB, null);
        final String expectedMessage = "currency and balance must be both null or both not null";
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void init_clearsPortfolio() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;

        service.init(dateTime, currency, BigDecimal.valueOf(1000000));
        final String ticker = "ticker";
        final int lotSize = 10;

        Mocker.createAndMockInstrument(realTinkoffService, ticker, lotSize);

        placeMarketOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(100));
        service.nextMinute();
        placeMarketOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(200));
        service.nextMinute();
        placeMarketOrder(ticker, 1, OperationType.SELL, BigDecimal.valueOf(300));

        final OffsetDateTime newDateTime = DateTimeTestData.createDateTime(2021, 10, 5, 12);
        final BigDecimal newBalance = BigDecimal.valueOf(1000);

        service.init(newDateTime, currency, newBalance);

        Assertions.assertEquals(newDateTime, service.getCurrentDateTime());
        Assertions.assertEquals(newBalance, service.getCurrentBalance(currency));

        final Interval interval = Interval.of(dateTime, newDateTime);
        Assertions.assertTrue(service.getOperations(interval, ticker).isEmpty());

        final SortedMap<OffsetDateTime, BigDecimal> investments = service.getInvestments(currency);
        Assertions.assertEquals(1, investments.size());
        AssertUtils.assertEquals(newBalance, investments.get(newDateTime));

        Assertions.assertTrue(service.getPortfolioPositions().isEmpty());
    }

    // endregion

    // region nextMinute tests

    @Test
    void nextMinute_movesToNextMinute_whenMiddleOfWorkDay() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);

        service.init(dateTime);

        final OffsetDateTime nextMinuteDateTime = service.nextMinute();

        final OffsetDateTime expected = dateTime.plusMinutes(1);
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, service.getCurrentDateTime());
    }

    @Test
    void nextMinute_movesToStartOfNextDay_whenAtEndOfWorkDay() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 18, 59, 59);

        service.init(dateTime);

        final OffsetDateTime nextMinuteDateTime = service.nextMinute();

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), TRADING_PROPERTIES.getWorkStartTime());
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, service.getCurrentDateTime());
    }

    @Test
    void nextMinute_movesToStartOfNextWeek_whenEndOfWorkWeek() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 9, 18, 59, 59);

        service.init(dateTime);

        final OffsetDateTime nextMinuteDateTime = service.nextMinute();

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(3), TRADING_PROPERTIES.getWorkStartTime());
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, service.getCurrentDateTime());
    }

    // endregion

    // region getOperations tests

    @Test
    void getOperations_filtersOperationsByInterval() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BigDecimal balance = BigDecimal.valueOf(1000000);

        service.init(dateTime, Currency.RUB, balance);

        final String ticker = "ticker";
        final int lotSize = 10;

        Mocker.createAndMockInstrument(realTinkoffService, ticker, lotSize);

        placeMarketOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(100));
        service.nextMinute();
        placeMarketOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(200));
        service.nextMinute();
        placeMarketOrder(ticker, 1, OperationType.SELL, BigDecimal.valueOf(300));

        final Interval wholeInterval = Interval.of(dateTime, dateTime.plusMinutes(2));
        final List<Operation> allOperations = service.getOperations(wholeInterval, ticker);

        Assertions.assertEquals(3, allOperations.size());

        final Interval localInterval = Interval.of(dateTime.plusMinutes(1), dateTime.plusMinutes(1));
        final List<Operation> localOperations = service.getOperations(localInterval, ticker);
        Assertions.assertEquals(1, localOperations.size());
        Assertions.assertEquals(dateTime.plusMinutes(1), localOperations.get(0).getDate());
    }

    @Test
    void getOperations_filtersOperationsByTicker_whenTickerIsNotNull() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BigDecimal balance = BigDecimal.valueOf(1000000);

        service.init(dateTime, Currency.RUB, balance);

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final int lotSize = 10;

        Mocker.createAndMockInstrument(realTinkoffService, ticker1, lotSize);
        Mocker.createAndMockInstrument(realTinkoffService, ticker2, lotSize);

        placeMarketOrder(ticker1, 1, OperationType.BUY, BigDecimal.valueOf(100));
        service.nextMinute();
        placeMarketOrder(ticker2, 1, OperationType.BUY, BigDecimal.valueOf(200));
        service.nextMinute();
        placeMarketOrder(ticker2, 1, OperationType.SELL, BigDecimal.valueOf(300));

        final Interval interval = Interval.of(dateTime, dateTime.plusMinutes(2));
        final List<Operation> ticker1Operations = service.getOperations(interval, ticker1);
        Assertions.assertEquals(1, ticker1Operations.size());
        Assertions.assertEquals(dateTime, ticker1Operations.get(0).getDate());

        final List<Operation> ticker2Operations = service.getOperations(interval, ticker2);
        Assertions.assertEquals(2, ticker2Operations.size());
        Assertions.assertEquals(dateTime.plusMinutes(1), ticker2Operations.get(0).getDate());
        Assertions.assertEquals(dateTime.plusMinutes(2), ticker2Operations.get(1).getDate());
    }

    @Test
    void getOperations_doesNotFilterOperationsByTicker_whenTickerIsNull() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BigDecimal balance = BigDecimal.valueOf(1000000);

        service.init(dateTime, Currency.RUB, balance);

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final int lotSize = 10;

        Mocker.createAndMockInstrument(realTinkoffService, ticker1, lotSize);
        Mocker.createAndMockInstrument(realTinkoffService, ticker2, lotSize);

        placeMarketOrder(ticker1, 1, OperationType.BUY, BigDecimal.valueOf(100));
        service.nextMinute();
        placeMarketOrder(ticker2, 1, OperationType.BUY, BigDecimal.valueOf(200));
        service.nextMinute();
        placeMarketOrder(ticker2, 1, OperationType.SELL, BigDecimal.valueOf(300));

        final Interval interval = Interval.of(dateTime, dateTime.plusMinutes(2));
        final List<Operation> operations = service.getOperations(interval, null);

        Assertions.assertEquals(3, operations.size());
    }

    // endregion

    // region placeMarketOrder tests. Implicit tests for getPortfolioPositions

    @Test
    void placeMarketOrder_buy_throwsIllegalArgumentException_whenNotEnoughBalance() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BigDecimal balance = BigDecimal.valueOf(1000);
        final Currency currency = Currency.RUB;

        service.init(dateTime, currency, balance);

        final String ticker = "ticker";

        Mocker.createAndMockInstrument(realTinkoffService, ticker, 10);

        final Executable executable = () -> placeMarketOrder(ticker, 2, OperationType.BUY, BigDecimal.valueOf(500));
        Assertions.assertThrows(IllegalArgumentException.class, executable, "balance can't be negative");

        Assertions.assertTrue(service.getPortfolioPositions().isEmpty());
        AssertUtils.assertEquals(1000, service.getCurrentBalance(currency));
    }

    @Test
    void placeMarketOrder_buy_createsNewPosition_whenNoPositions() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BigDecimal balance = BigDecimal.valueOf(1000000);
        final Currency currency = Currency.RUB;

        service.init(dateTime, currency, balance);

        final String ticker = "ticker";

        Mocker.createAndMockInstrument(realTinkoffService, ticker, 10);

        placeMarketOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(1000));

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions();
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.getTicker());
        Assertions.assertEquals(10, portfolioPosition.getCount());
        AssertUtils.assertEquals(989970, service.getCurrentBalance(currency));
    }

    @Test
    void placeMarketOrder_buy_addsValueToExistingPosition_whenPositionAlreadyExists() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BigDecimal balance = BigDecimal.valueOf(1000000);
        final Currency currency = Currency.RUB;

        service.init(dateTime, currency, balance);

        final String ticker = "ticker";

        Mocker.createAndMockInstrument(realTinkoffService, ticker, 10);

        placeMarketOrder(ticker, 2, OperationType.BUY, BigDecimal.valueOf(1000));
        placeMarketOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(4000));

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions();
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.getTicker());
        Assertions.assertEquals(30, portfolioPosition.getCount());
        AssertUtils.assertEquals(2000, portfolioPosition.getAveragePositionPrice());
        AssertUtils.assertEquals(939820, service.getCurrentBalance(currency));
    }

    @Test
    void placeMarketOrder_buy_createsMultiplePositions_whenDifferentTickersAreBought() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(1000000);

        service.init(dateTime, currency, balance);

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final String ticker3 = "ticker3";

        Mocker.createAndMockInstrument(realTinkoffService, ticker1, 10);
        Mocker.createAndMockInstrument(realTinkoffService, ticker2, 2);
        Mocker.createAndMockInstrument(realTinkoffService, ticker3, 1);

        placeMarketOrder(ticker1, 1, OperationType.BUY, BigDecimal.valueOf(1000));
        placeMarketOrder(ticker2, 3, OperationType.BUY, BigDecimal.valueOf(100));
        placeMarketOrder(ticker3, 1, OperationType.BUY, BigDecimal.valueOf(500));

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions();
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

        AssertUtils.assertEquals(988866.7, service.getCurrentBalance(currency));
    }

    @Test
    void placeMarketOrder_sell_throwsIllegalArgumentException_whenSellsMoreLotsThanExists() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BigDecimal balance = BigDecimal.valueOf(1000000);
        final Currency currency = Currency.RUB;
        final int lotSize = 10;

        service.init(dateTime, currency, balance);

        final String ticker = "ticker";

        Mocker.createAndMockInstrument(realTinkoffService, ticker, lotSize);

        placeMarketOrder(ticker, 2, OperationType.BUY, BigDecimal.valueOf(1000));
        placeMarketOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(4000));
        final Executable sellExecutable = () -> placeMarketOrder(ticker, 4, OperationType.SELL, BigDecimal.valueOf(3000));
        final String expectedMessage = "lotsCount 4 can't be greater than existing position lots count 3";
        Assertions.assertThrows(IllegalArgumentException.class, sellExecutable, expectedMessage);

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions();
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.getTicker());
        Assertions.assertEquals(30, portfolioPosition.getCount());
        AssertUtils.assertEquals(2000, portfolioPosition.getAveragePositionPrice());
        AssertUtils.assertEquals(939820, service.getCurrentBalance(currency));
    }

    @Test
    void placeMarketOrder_sell_removesPosition_whenAllLotsAreSold() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BigDecimal balance = BigDecimal.valueOf(1000000);
        final Currency currency = Currency.RUB;
        final int lotSize = 10;

        service.init(dateTime, currency, balance);

        final String ticker = "ticker";

        Mocker.createAndMockInstrument(realTinkoffService, ticker, lotSize);

        placeMarketOrder(ticker, 2, OperationType.BUY, BigDecimal.valueOf(1000));
        placeMarketOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(4000));
        placeMarketOrder(ticker, 3, OperationType.SELL, BigDecimal.valueOf(3000));

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions();
        Assertions.assertTrue(positions.isEmpty());
        AssertUtils.assertEquals(1029550, service.getCurrentBalance(currency));
    }

    @Test
    void placeMarketOrder_sell_reducesLotsCount() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final BigDecimal balance = BigDecimal.valueOf(1000000);
        final Currency currency = Currency.RUB;
        final int lotSize = 10;

        service.init(dateTime, currency, balance);

        final String ticker = "ticker";

        Mocker.createAndMockInstrument(realTinkoffService, ticker, lotSize);

        placeMarketOrder(ticker, 2, OperationType.BUY, BigDecimal.valueOf(1000));
        placeMarketOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(4000));
        placeMarketOrder(ticker, 1, OperationType.SELL, BigDecimal.valueOf(3000));

        final Collection<PortfolioPosition> positions = service.getPortfolioPositions();
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.getTicker());
        Assertions.assertEquals(20, portfolioPosition.getCount());
        AssertUtils.assertEquals(2000, portfolioPosition.getAveragePositionPrice());
        AssertUtils.assertEquals(969730, service.getCurrentBalance(currency));
    }

    // endregion

    // region getPortfolioCurrencies tests

    @Test
    void getPortfolioCurrencies_returnsAllCurrencies_whenCurrenciesAreNotInitialized() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);

        service.init(dateTime);

        final List<CurrencyPosition> currencies = service.getPortfolioCurrencies();

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

    @Test
    void getPortfolioCurrencies_returnsCurrencyWithBalance_whenBalanceInitialized() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(1000);

        service.init(dateTime, currency, balance);

        final List<CurrencyPosition> currencies = service.getPortfolioCurrencies();

        CurrencyPosition portfolioCurrency = currencies.stream()
                .filter(currentPortfolioCurrency -> currentPortfolioCurrency.getCurrency() == currency)
                .findFirst()
                .orElseThrow();

        AssertUtils.assertEquals(balance, portfolioCurrency.getBalance());
    }

    @Test
    void getPortfolioCurrencies_returnsCurrencyWithBalance_afterIncrementBalance() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(1000);

        service.init(dateTime);
        service.incrementBalance(currency, balance);

        final List<CurrencyPosition> currencies = service.getPortfolioCurrencies();

        CurrencyPosition portfolioCurrency = currencies.stream()
                .filter(currentPortfolioCurrency -> currentPortfolioCurrency.getCurrency() == currency)
                .findFirst()
                .orElseThrow();

        AssertUtils.assertEquals(balance, portfolioCurrency.getBalance());
    }

    // endregion

    private void placeMarketOrder(String ticker, int lots, OperationType operationType, BigDecimal price) {
        final Candle candle = new Candle().setOpenPrice(price);
        Mockito.when(marketService.getLastCandle(ticker, service.getCurrentDateTime())).thenReturn(candle);

        MarketOrderRequest orderRequest = new MarketOrderRequest()
                .lots(lots)
                .operation(operationType);
        service.placeMarketOrder(ticker, orderRequest);
    }

}