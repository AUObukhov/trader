package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import ru.obukhov.trader.BaseMockedTest;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;
import ru.tinkoff.invest.openapi.model.rest.MarketOrderRequest;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationType;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

@RunWith(MockitoJUnitRunner.class)
class FakeTinkoffServiceTest extends BaseMockedTest {

    private static TradingProperties tradingProperties;

    @Mock
    private MarketService marketService;
    @Mock
    private RealTinkoffService realTinkoffService;

    private FakeTinkoffService service;

    @BeforeAll
    static void setUpAll() {
        tradingProperties = new TradingProperties();
        tradingProperties.setConsecutiveEmptyDaysLimit(7);
        tradingProperties.setWorkStartTime(DateUtils.getTime(10, 0, 0).toOffsetTime());
        tradingProperties.setWorkDuration(Duration.ofHours(9));
        tradingProperties.setCommission(0.001);
    }

    @BeforeEach
    void setUpEach() {
        this.service = new FakeTinkoffService(tradingProperties, marketService, realTinkoffService);
    }

    // region init tests

    @Test
    void init_setsCurrentMinuteToCurrentDateTime_whenMiddleOfWorkDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);

        service.init(dateTime);

        Assertions.assertEquals(dateTime, service.getCurrentDateTime());
    }

    @Test
    void init_setsStartOfNextDayToCurrentDateTime_whenAtEndOfWorkDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 19, 0, 0);

        service.init(dateTime);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), tradingProperties.getWorkStartTime());
        Assertions.assertEquals(expected, service.getCurrentDateTime());
    }

    @Test
    void init_setsStartOfNextDayToCurrentDateTime_whenAfterEndOfWorkDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 19, 20, 0);

        service.init(dateTime);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), tradingProperties.getWorkStartTime());
        Assertions.assertEquals(expected, service.getCurrentDateTime());
    }

    @Test
    void init_setsStartOfNextWeekToCurrentDateTime_whenEndOfWorkWeek() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 9, 19, 0, 0);

        service.init(dateTime);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(3), tradingProperties.getWorkStartTime());
        Assertions.assertEquals(expected, service.getCurrentDateTime());
    }

    @Test
    void init_setsStartOfNextWeekToCurrentDateTime_whenAtWeekend() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 12, 0, 0);

        service.init(dateTime);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(2), tradingProperties.getWorkStartTime());
        Assertions.assertEquals(expected, service.getCurrentDateTime());
    }

    @Test
    void init_setsStartOfTodayWorkDayToCurrentDateTime_whenBeforeStartOfTodayWorkDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 9, 9, 0, 0);

        service.init(dateTime);

        OffsetDateTime expected = DateUtils.setTime(dateTime, tradingProperties.getWorkStartTime());
        Assertions.assertEquals(expected, service.getCurrentDateTime());
    }

    @Test
    void init_clearsPortfolio() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final Currency currency = Currency.RUB;

        service.init(dateTime, currency, BigDecimal.valueOf(1000000));
        final String ticker = "ticker";

        TestDataHelper.createAndMockInstrument(realTinkoffService, ticker);

        placeOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(100));
        service.nextMinute();
        placeOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(200));
        service.nextMinute();
        placeOrder(ticker, 1, OperationType.SELL, BigDecimal.valueOf(300));

        OffsetDateTime newDateTime = DateUtils.getDateTime(2021, 10, 5, 12, 0, 0);
        BigDecimal newBalance = BigDecimal.valueOf(1000);

        service.init(newDateTime, currency, newBalance);

        Assertions.assertEquals(newDateTime, service.getCurrentDateTime());
        Assertions.assertEquals(newBalance, service.getCurrentBalance(currency));

        Interval interval = Interval.of(dateTime, newDateTime);
        Assertions.assertTrue(service.getOperations(interval, ticker).isEmpty());

        SortedMap<OffsetDateTime, BigDecimal> investments = service.getInvestments(currency);
        Assertions.assertEquals(1, investments.size());
        AssertUtils.assertEquals(newBalance, investments.get(newDateTime));

        Assertions.assertTrue(service.getPortfolioPositions().isEmpty());
    }

    // endregion

    // region nextMinute tests

    @Test
    void nextMinute_movesToNextMinute_whenMiddleOfWorkDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);

        service.init(dateTime);

        OffsetDateTime nextMinuteDateTime = service.nextMinute();

        OffsetDateTime expected = dateTime.plusMinutes(1);
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, service.getCurrentDateTime());
    }

    @Test
    void nextMinute_movesToStartOfNextDay_whenAtEndOfWorkDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 18, 59, 59);

        service.init(dateTime);

        OffsetDateTime nextMinuteDateTime = service.nextMinute();

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), tradingProperties.getWorkStartTime());
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, service.getCurrentDateTime());
    }

    @Test
    void nextMinute_movesToStartOfNextWeek_whenEndOfWorkWeek() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 9, 18, 59, 59);

        service.init(dateTime);

        OffsetDateTime nextMinuteDateTime = service.nextMinute();

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(3), tradingProperties.getWorkStartTime());
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, service.getCurrentDateTime());
    }

    // endregion

    // region getOperations tests

    @Test
    void getOperations_filtersOperationsByInterval() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final BigDecimal balance = BigDecimal.valueOf(1000000);

        service.init(dateTime, Currency.RUB, balance);

        final String ticker = "ticker";

        TestDataHelper.createAndMockInstrument(realTinkoffService, ticker);

        placeOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(100));
        service.nextMinute();
        placeOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(200));
        service.nextMinute();
        placeOrder(ticker, 1, OperationType.SELL, BigDecimal.valueOf(300));

        Interval wholeInterval = Interval.of(dateTime, dateTime.plusMinutes(2));
        List<Operation> allOperations = service.getOperations(wholeInterval, ticker);

        Assertions.assertEquals(3, allOperations.size());

        Interval localInterval = Interval.of(dateTime.plusMinutes(1), dateTime.plusMinutes(1));
        List<Operation> localOperations = service.getOperations(localInterval, ticker);
        Assertions.assertEquals(1, localOperations.size());
        Assertions.assertEquals(dateTime.plusMinutes(1), localOperations.get(0).getDate());
    }

    @Test
    void getOperations_filtersOperationsByTicker_whenTickerIsNotNull() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final BigDecimal balance = BigDecimal.valueOf(1000000);

        service.init(dateTime, Currency.RUB, balance);

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";

        TestDataHelper.createAndMockInstrument(realTinkoffService, ticker1);
        TestDataHelper.createAndMockInstrument(realTinkoffService, ticker2);

        placeOrder(ticker1, 1, OperationType.BUY, BigDecimal.valueOf(100));
        service.nextMinute();
        placeOrder(ticker2, 1, OperationType.BUY, BigDecimal.valueOf(200));
        service.nextMinute();
        placeOrder(ticker2, 1, OperationType.SELL, BigDecimal.valueOf(300));

        Interval interval = Interval.of(dateTime, dateTime.plusMinutes(2));
        List<Operation> ticker1Operations = service.getOperations(interval, ticker1);
        Assertions.assertEquals(1, ticker1Operations.size());
        Assertions.assertEquals(dateTime, ticker1Operations.get(0).getDate());

        List<Operation> ticker2Operations = service.getOperations(interval, ticker2);
        Assertions.assertEquals(2, ticker2Operations.size());
        Assertions.assertEquals(dateTime.plusMinutes(1), ticker2Operations.get(0).getDate());
        Assertions.assertEquals(dateTime.plusMinutes(2), ticker2Operations.get(1).getDate());
    }

    @Test
    void getOperations_doesNotFilterOperationsByTicker_whenTickerIsNull() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final BigDecimal balance = BigDecimal.valueOf(1000000);

        service.init(dateTime, Currency.RUB, balance);

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";

        TestDataHelper.createAndMockInstrument(realTinkoffService, ticker1);
        TestDataHelper.createAndMockInstrument(realTinkoffService, ticker2);

        placeOrder(ticker1, 1, OperationType.BUY, BigDecimal.valueOf(100));
        service.nextMinute();
        placeOrder(ticker2, 1, OperationType.BUY, BigDecimal.valueOf(200));
        service.nextMinute();
        placeOrder(ticker2, 1, OperationType.SELL, BigDecimal.valueOf(300));

        Interval interval = Interval.of(dateTime, dateTime.plusMinutes(2));
        List<Operation> operations = service.getOperations(interval, null);

        Assertions.assertEquals(3, operations.size());
    }

    // endregion

    // region placeMarketOrder tests. Implicit tests for getPortfolioPositions

    @Test
    void placeMarketOrder_buy_throwsIllegalArgumentException_whenNotEnoughBalance() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final BigDecimal balance = BigDecimal.valueOf(1000);
        final Currency currency = Currency.RUB;

        service.init(dateTime, currency, balance);

        String ticker = "ticker";

        TestDataHelper.createAndMockInstrument(realTinkoffService, ticker);

        Executable executable = () ->
                placeOrder(ticker,
                        2,
                        OperationType.BUY,
                        BigDecimal.valueOf(500));

        AssertUtils.assertThrowsWithMessage(executable,
                IllegalArgumentException.class,
                "balance can't be negative");

        Assertions.assertTrue(service.getPortfolioPositions().isEmpty());
        AssertUtils.assertEquals(BigDecimal.valueOf(1000), service.getCurrentBalance(currency));
    }

    @Test
    void placeMarketOrder_buy_createsNewPosition_whenNoPositions() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final BigDecimal balance = BigDecimal.valueOf(1000000);
        final Currency currency = Currency.RUB;

        service.init(dateTime, currency, balance);

        String ticker = "ticker";

        TestDataHelper.createAndMockInstrument(realTinkoffService, ticker);

        placeOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(1000));

        Collection<PortfolioPosition> positions = service.getPortfolioPositions();
        Assertions.assertEquals(1, positions.size());
        Assertions.assertEquals(ticker, positions.iterator().next().getTicker());
        AssertUtils.assertEquals(BigDecimal.valueOf(998999), service.getCurrentBalance(currency));
    }

    @Test
    void placeMarketOrder_buy_addValueToExistingPosition_whenPositionAlreadyExists() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final BigDecimal balance = BigDecimal.valueOf(1000000);
        final Currency currency = Currency.RUB;

        service.init(dateTime, currency, balance);

        String ticker = "ticker";

        TestDataHelper.createAndMockInstrument(realTinkoffService, ticker);

        placeOrder(ticker, 2, OperationType.BUY, BigDecimal.valueOf(1000));
        placeOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(4000));

        Collection<PortfolioPosition> positions = service.getPortfolioPositions();
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.getTicker());
        Assertions.assertEquals(3, portfolioPosition.getLotsCount());
        AssertUtils.assertEquals(BigDecimal.valueOf(2000), portfolioPosition.getAveragePositionPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(993994), service.getCurrentBalance(currency));
    }

    @Test
    void placeMarketOrder_sell_throwsIllegalArgumentException_whenSellsMoreLotsThanExists() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final BigDecimal balance = BigDecimal.valueOf(1000000);
        final Currency currency = Currency.RUB;

        service.init(dateTime, currency, balance);

        String ticker = "ticker";

        TestDataHelper.createAndMockInstrument(realTinkoffService, ticker);

        placeOrder(ticker, 2, OperationType.BUY, BigDecimal.valueOf(1000));
        placeOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(4000));
        Executable sellExecutable = () -> placeOrder(ticker,
                4,
                OperationType.SELL,
                BigDecimal.valueOf(3000));

        AssertUtils.assertThrowsWithMessage(sellExecutable,
                IllegalArgumentException.class,
                "lotsCount 4 can't be greater than existing position lots count 3");

        Collection<PortfolioPosition> positions = service.getPortfolioPositions();
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.getTicker());
        Assertions.assertEquals(3, portfolioPosition.getLotsCount());
        AssertUtils.assertEquals(BigDecimal.valueOf(2000), portfolioPosition.getAveragePositionPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(993994), service.getCurrentBalance(currency));
    }

    @Test
    void placeMarketOrder_sell_removesPosition_whenAllLotsSold() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final BigDecimal balance = BigDecimal.valueOf(1000000);
        final Currency currency = Currency.RUB;

        service.init(dateTime, currency, balance);

        String ticker = "ticker";

        TestDataHelper.createAndMockInstrument(realTinkoffService, ticker);

        placeOrder(ticker, 2, OperationType.BUY, BigDecimal.valueOf(1000));
        placeOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(4000));
        placeOrder(ticker, 3, OperationType.SELL, BigDecimal.valueOf(3000));

        Collection<PortfolioPosition> positions = service.getPortfolioPositions();
        Assertions.assertTrue(positions.isEmpty());
        AssertUtils.assertEquals(BigDecimal.valueOf(1002985), service.getCurrentBalance(currency));
    }

    @Test
    void placeMarketOrder_sell_reducesLotsCount() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final BigDecimal balance = BigDecimal.valueOf(1000000);
        final Currency currency = Currency.RUB;

        service.init(dateTime, currency, balance);

        String ticker = "ticker";

        TestDataHelper.createAndMockInstrument(realTinkoffService, ticker);

        placeOrder(ticker, 2, OperationType.BUY, BigDecimal.valueOf(1000));
        placeOrder(ticker, 1, OperationType.BUY, BigDecimal.valueOf(4000));
        placeOrder(ticker, 1, OperationType.SELL, BigDecimal.valueOf(3000));

        Collection<PortfolioPosition> positions = service.getPortfolioPositions();
        Assertions.assertEquals(1, positions.size());
        final PortfolioPosition portfolioPosition = positions.iterator().next();
        Assertions.assertEquals(ticker, portfolioPosition.getTicker());
        Assertions.assertEquals(2, portfolioPosition.getLotsCount());
        AssertUtils.assertEquals(BigDecimal.valueOf(2000), portfolioPosition.getAveragePositionPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(996991), service.getCurrentBalance(currency));
    }

    // endregion

    // region getPortfolioCurrencies tests

    @Test
    void getPortfolioCurrencies_returnsAllCurrencies_whenCurrenciesAreNotInitialized() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);

        service.init(dateTime);

        List<CurrencyPosition> currencies = service.getPortfolioCurrencies();

        Assertions.assertEquals(Currency.values().length, currencies.size());
        for (Currency currency : Currency.values()) {
            if (currencies.stream().noneMatch(portfolioCurrency -> portfolioCurrency.getCurrency() == currency)) {
                Assertions.fail("Currency " + currency + " found in getPortfolioCurrencies result");
            }
        }

        for (CurrencyPosition portfolioCurrency : currencies) {
            AssertUtils.assertEquals(BigDecimal.ZERO, portfolioCurrency.getBalance());
            Assertions.assertNull(portfolioCurrency.getBlocked());
        }
    }

    @Test
    void getPortfolioCurrencies_returnsCurrencyWithBalance_whenBalanceInitialized() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(1000);

        service.init(dateTime, currency, balance);

        List<CurrencyPosition> currencies = service.getPortfolioCurrencies();

        CurrencyPosition portfolioCurrency = currencies.stream()
                .filter(currentPortfolioCurrency -> currentPortfolioCurrency.getCurrency() == currency)
                .findFirst()
                .orElseThrow();

        AssertUtils.assertEquals(balance, portfolioCurrency.getBalance());
    }

    @Test
    void getPortfolioCurrencies_returnsCurrencyWithBalance_afterIncrementBalance() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(1000);

        service.init(dateTime);
        service.incrementBalance(currency, balance);

        List<CurrencyPosition> currencies = service.getPortfolioCurrencies();

        CurrencyPosition portfolioCurrency = currencies.stream()
                .filter(currentPortfolioCurrency -> currentPortfolioCurrency.getCurrency() == currency)
                .findFirst()
                .orElseThrow();

        AssertUtils.assertEquals(balance, portfolioCurrency.getBalance());
    }

    // endregion

    private void placeOrder(String ticker,
                            int lots,
                            OperationType operationType,
                            BigDecimal price) {
        Candle candle = TestDataHelper.createCandleWithOpenPrice(price);
        Mockito.when(marketService.getLastCandle(ticker, service.getCurrentDateTime())).thenReturn(candle);

        MarketOrderRequest orderRequest = new MarketOrderRequest();
        orderRequest.setLots(lots);
        orderRequest.setOperation(operationType);
        service.placeMarketOrder(ticker, orderRequest);
    }

}