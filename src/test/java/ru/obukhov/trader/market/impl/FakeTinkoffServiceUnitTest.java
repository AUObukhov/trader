package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.FakeContext;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@ExtendWith(MockitoExtension.class)
class FakeTinkoffServiceUnitTest {

    private static final MarketProperties MARKET_PROPERTIES = TestData.createMarketProperties();

    @Mock
    private ExtMarketDataService extMarketDataService;
    @Mock
    private ExtInstrumentsService extInstrumentsService;
    @InjectMocks
    private TinkoffServices tinkoffServices;

    private FakeTinkoffService fakeTinkoffService;

    // region nextMinute tests

    @Test
    void nextMinute_movesToNextMinute_whenMiddleOfWorkDay() {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final FakeContext fakeContext = new FakeContext(dateTime, accountId, Currency.USD, BigDecimal.ZERO);

        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext, 0.003);

        final OffsetDateTime nextMinuteDateTime = fakeTinkoffService.nextMinute();

        final OffsetDateTime expected = dateTime.plusMinutes(1);
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, fakeTinkoffService.getCurrentDateTime());
    }

    @Test
    void nextMinute_movesToStartOfNextDay_whenAtEndOfWorkDay() {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 18, 59, 59);
        final FakeContext fakeContext = new FakeContext(dateTime, accountId, Currency.USD, BigDecimal.ZERO);

        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext, 0.003);

        final OffsetDateTime nextMinuteDateTime = fakeTinkoffService.nextMinute();

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), MARKET_PROPERTIES.getWorkSchedule().getStartTime());
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, fakeTinkoffService.getCurrentDateTime());
    }

    @Test
    void nextMinute_movesToStartOfNextWeek_whenEndOfWorkWeek() {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 9, 18, 59, 59);
        final FakeContext fakeContext = new FakeContext(dateTime, accountId, Currency.USD, BigDecimal.ZERO);

        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext, 0.003);

        final OffsetDateTime nextMinuteDateTime = fakeTinkoffService.nextMinute();

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(3), MARKET_PROPERTIES.getWorkSchedule().getStartTime());
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, fakeTinkoffService.getCurrentDateTime());
    }

    // endregion

    // region placeMarketOrder tests. Implicit tests for getPortfolioPositions

    // todo move these tests to new classes with postOrder
//    @Test
//    void placeMarketOrder_buy_throwsIllegalArgumentException_whenNotEnoughBalance() {
//        final String accountId = "2000124699";
//        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
//        final Currency currency = Currency.RUB;
//        final FakeContext fakeContext = new FakeContext(dateTime, accountId, currency, BigDecimal.valueOf(1000.0));
//
//        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext, 0.003);
//
//        final String ticker = "ticker";
//
//        Mocker.mockShare(extInstrumentsService, ticker, Currency.RUB, 10);
//
//        final Executable executable = () -> postOrder(accountId, ticker, 2, OrderDirection.ORDER_DIRECTION_BUY, 500);
//        Assertions.assertThrows(IllegalArgumentException.class, executable, "balance can't be negative");
//
//        Assertions.assertTrue(fakeTinkoffService.getPortfolioPositions(accountId).isEmpty());
//        AssertUtils.assertEquals(1000, fakeTinkoffService.getCurrentBalance(accountId, currency));
//    }
//
//    @Test
//    void placeMarketOrder_buy_createsNewPosition_whenNoPositions() throws IOException {
//        final String accountId = "2000124699";
//        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
//        final Currency currency = Currency.RUB;
//        final FakeContext fakeContext = new FakeContext(dateTime, accountId, currency, BigDecimal.valueOf(1000000.0));
//
//        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext, 0.003);
//
//        final String ticker = "ticker";
//
//        Mocker.mockShare(extInstrumentsService, ticker, Currency.RUB, 10);
//
//        postOrder(accountId, ticker, 1, OrderDirection.ORDER_DIRECTION_BUY, 1000);
//
//        final Collection<PortfolioPosition> positions = fakeTinkoffService.getPortfolioPositions(accountId);
//        Assertions.assertEquals(1, positions.size());
//        final PortfolioPosition portfolioPosition = positions.iterator().next();
//        Assertions.assertEquals(ticker, portfolioPosition.ticker());
//        AssertUtils.assertEquals(10, portfolioPosition.quantity());
//        AssertUtils.assertEquals(989970, fakeTinkoffService.getCurrentBalance(accountId, currency));
//    }
//
//    @Test
//    void placeMarketOrder_buy_addsValueToExistingPosition_whenPositionAlreadyExists() throws IOException {
//        final String accountId = "2000124699";
//        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
//        final Currency currency = Currency.RUB;
//        final FakeContext fakeContext = new FakeContext(dateTime, accountId, currency, BigDecimal.valueOf(1000000.0));
//
//        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext, 0.003);
//
//        final String ticker = "ticker";
//
//        Mocker.mockShare(extInstrumentsService, ticker, Currency.RUB, 10);
//
//        postOrder(accountId, ticker, 2, OrderDirection.ORDER_DIRECTION_BUY, 1000);
//        postOrder(accountId, ticker, 1, OrderDirection.ORDER_DIRECTION_BUY, 4000);
//
//        final Collection<PortfolioPosition> positions = fakeTinkoffService.getPortfolioPositions(accountId);
//        Assertions.assertEquals(1, positions.size());
//        final PortfolioPosition portfolioPosition = positions.iterator().next();
//        Assertions.assertEquals(ticker, portfolioPosition.ticker());
//        AssertUtils.assertEquals(30, portfolioPosition.quantity());
//        AssertUtils.assertEquals(2000, portfolioPosition.averagePositionPrice().value());
//        AssertUtils.assertEquals(939820, fakeTinkoffService.getCurrentBalance(accountId, currency));
//    }
//
//    @Test
//    void placeMarketOrder_buy_createsMultiplePositions_whenDifferentTickersAreBought() throws IOException {
//        final String accountId = "2000124699";
//        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
//        final Currency currency = Currency.RUB;
//        final FakeContext fakeContext = new FakeContext(dateTime, accountId, currency, BigDecimal.valueOf(1000000.0));
//
//        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext, 0.003);
//
//        final String ticker1 = "ticker1";
//        final String ticker2 = "ticker2";
//        final String ticker3 = "ticker3";
//
//        Mocker.mockShare(extInstrumentsService, ticker1, Currency.RUB, 10);
//        Mocker.mockShare(extInstrumentsService, ticker2, Currency.RUB, 2);
//        Mocker.mockShare(extInstrumentsService, ticker3, Currency.RUB, 1);
//
//        postOrder(accountId, ticker1, 1, OrderDirection.ORDER_DIRECTION_BUY, 1000);
//        postOrder(accountId, ticker2, 3, OrderDirection.ORDER_DIRECTION_BUY, 100);
//        postOrder(accountId, ticker3, 1, OrderDirection.ORDER_DIRECTION_BUY, 500);
//
//        final Collection<PortfolioPosition> positions = fakeTinkoffService.getPortfolioPositions(accountId);
//        Assertions.assertEquals(3, positions.size());
//
//        final Iterator<PortfolioPosition> positionIterator = positions.iterator();
//        final PortfolioPosition portfolioPosition1 = positionIterator.next();
//        Assertions.assertEquals(ticker1, portfolioPosition1.ticker());
//        AssertUtils.assertEquals(10, portfolioPosition1.quantity());
//
//        final PortfolioPosition portfolioPosition2 = positionIterator.next();
//        Assertions.assertEquals(ticker2, portfolioPosition2.ticker());
//        AssertUtils.assertEquals(6, portfolioPosition2.quantity());
//
//        final PortfolioPosition portfolioPosition3 = positionIterator.next();
//        Assertions.assertEquals(ticker3, portfolioPosition3.ticker());
//        AssertUtils.assertEquals(1, portfolioPosition3.quantity());
//
//        AssertUtils.assertEquals(988866.7, fakeTinkoffService.getCurrentBalance(accountId, currency));
//    }
//
//    @Test
//    void placeMarketOrder_sell_throwsIllegalArgumentException_whenSellsMoreLotsThanExists() throws IOException {
//        final String accountId = "2000124699";
//        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
//        final Currency currency = Currency.RUB;
//        final FakeContext fakeContext = new FakeContext(dateTime, accountId, currency, BigDecimal.valueOf(1000000.0));
//
//        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext, 0.003);
//        final int lotSize = 10;
//
//        final String ticker = "ticker";
//
//        Mocker.mockShare(extInstrumentsService, ticker, Currency.RUB, lotSize);
//
//        postOrder(accountId, ticker, 2, OrderDirection.ORDER_DIRECTION_BUY, 1000);
//        postOrder(accountId, ticker, 1, OrderDirection.ORDER_DIRECTION_BUY, 4000);
//        final Executable sellExecutable = () -> postOrder(accountId, ticker, 4, OrderDirection.ORDER_DIRECTION_SELL, 3000);
//        final String expectedMessage = "lotsCount 4 can't be greater than existing position lots count 3";
//        Assertions.assertThrows(IllegalArgumentException.class, sellExecutable, expectedMessage);
//
//        final Collection<PortfolioPosition> positions = fakeTinkoffService.getPortfolioPositions(accountId);
//        Assertions.assertEquals(1, positions.size());
//        final PortfolioPosition portfolioPosition = positions.iterator().next();
//        Assertions.assertEquals(ticker, portfolioPosition.ticker());
//        AssertUtils.assertEquals(30, portfolioPosition.quantity());
//        AssertUtils.assertEquals(2000, portfolioPosition.averagePositionPrice().value());
//        AssertUtils.assertEquals(939820, fakeTinkoffService.getCurrentBalance(accountId, currency));
//    }
//
//    @Test
//    void placeMarketOrder_sell_removesPosition_whenAllLotsAreSold() throws IOException {
//        final String accountId = "2000124699";
//        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
//        final Currency currency = Currency.RUB;
//        final FakeContext fakeContext = new FakeContext(dateTime, accountId, currency, BigDecimal.valueOf(1000000.0));
//        final int lotSize = 10;
//        final String ticker = "ticker";
//
//        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext, 0.003);
//
//        Mocker.mockShare(extInstrumentsService, ticker, Currency.RUB, lotSize);
//
//        postOrder(accountId, ticker, 2, OrderDirection.ORDER_DIRECTION_BUY, 1000);
//        postOrder(accountId, ticker, 1, OrderDirection.ORDER_DIRECTION_BUY, 4000);
//        postOrder(accountId, ticker, 3, OrderDirection.ORDER_DIRECTION_SELL, 3000);
//
//        final Collection<PortfolioPosition> positions = fakeTinkoffService.getPortfolioPositions(accountId);
//        Assertions.assertTrue(positions.isEmpty());
//        AssertUtils.assertEquals(1029550, fakeTinkoffService.getCurrentBalance(accountId, currency));
//    }
//
//    @Test
//    void placeMarketOrder_sell_reducesLotsCount() throws IOException {
//        final String accountId = "2000124699";
//        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
//        final Currency currency = Currency.RUB;
//        final FakeContext fakeContext = new FakeContext(dateTime, accountId, currency, BigDecimal.valueOf(1000000.0));
//        final int lotSize = 10;
//        final String ticker = "ticker";
//
//        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext, 0.003);
//
//        Mocker.mockShare(extInstrumentsService, ticker, Currency.RUB, lotSize);
//
//        postOrder(accountId, ticker, 2, OrderDirection.ORDER_DIRECTION_BUY, 1000);
//        postOrder(accountId, ticker, 1, OrderDirection.ORDER_DIRECTION_BUY, 4000);
//        postOrder(accountId, ticker, 1, OrderDirection.ORDER_DIRECTION_SELL, 3000);
//
//        final Collection<PortfolioPosition> positions = fakeTinkoffService.getPortfolioPositions(accountId);
//        Assertions.assertEquals(1, positions.size());
//        final PortfolioPosition portfolioPosition = positions.iterator().next();
//        Assertions.assertEquals(ticker, portfolioPosition.ticker());
//        AssertUtils.assertEquals(20, portfolioPosition.quantity());
//        AssertUtils.assertEquals(2000, portfolioPosition.averagePositionPrice().value());
//        AssertUtils.assertEquals(969730, fakeTinkoffService.getCurrentBalance(accountId, currency));
//    }

    // endregion

    // region getWithdrawLimits tests

//    @Test
//    void getWithdrawLimits_returnsAllCurrencies_whenCurrenciesAreNotInitialized() {
//        final String accountId = "2000124699";
//
//        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
//        final Currency currency = Currency.USD;
//        final FakeContext fakeContext = new FakeContext(dateTime, accountId, currency, BigDecimal.ZERO);
//
//        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext, 0.003);
//
//        final WithdrawLimits withdrawLimits = fakeTinkoffService.getWithdrawLimits(accountId);
//
//        final List<Money> moneys = withdrawLimits.getMoney();
//        Assertions.assertEquals(1, moneys.size());
//        Assertions.assertEquals(currency.name(), moneys.get(0).getCurrency().getCurrencyCode());
//        for (final Money money : moneys) {
//            AssertUtils.assertEquals(0, money.getValue());
//        }
//
//        final List<Money> blocked = withdrawLimits.getBlocked();
//        Assertions.assertEquals(1, blocked.size());
//        Assertions.assertEquals(currency.name(), blocked.get(0).getCurrency().getCurrencyCode());
//        for (final Money money : blocked) {
//            AssertUtils.assertEquals(0, money.getValue());
//        }
//
//        final List<Money> blockedGuarantee = withdrawLimits.getBlockedGuarantee();
//        Assertions.assertEquals(1, blockedGuarantee.size());
//        Assertions.assertEquals(currency.name(), blockedGuarantee.get(0).getCurrency().getCurrencyCode());
//        for (final Money money : blockedGuarantee) {
//            AssertUtils.assertEquals(0, money.getValue());
//        }
//    }
//
//    @Test
//    void getWithdrawLimits_returnsCurrencyWithBalance_whenBalanceInitialized() {
//        final String accountId = "2000124699";
//
//        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
//        final Currency currency = Currency.RUB;
//        final BigDecimal initialBalance = BigDecimal.valueOf(1000.0);
//        final FakeContext fakeContext = new FakeContext(dateTime, accountId, currency, initialBalance);
//
//        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext, 0.003);
//
//        final WithdrawLimits withdrawLimits = fakeTinkoffService.getWithdrawLimits(accountId);
//
//        final BigDecimal balance = DataStructsHelper.getBalance(withdrawLimits.getMoney(), currency);
//        final BigDecimal blockedBalance = DataStructsHelper.getBalance(withdrawLimits.getBlocked(), currency);
//        final BigDecimal blockedGuaranteeBalance = DataStructsHelper.getBalance(withdrawLimits.getBlockedGuarantee(), currency);
//
//        AssertUtils.assertEquals(initialBalance, balance);
//        AssertUtils.assertEquals(0, blockedBalance);
//        AssertUtils.assertEquals(0, blockedGuaranteeBalance);
//    }
//
//    @Test
//    void getWithdrawLimits_returnsCurrencyWithBalance_afterAddInvestment() {
//        final String accountId = "2000124699";
//
//        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
//        final Currency currency = Currency.RUB;
//        final BigDecimal initialInvestment = BigDecimal.valueOf(1000);
//        final FakeContext fakeContext = new FakeContext(dateTime, accountId, currency, BigDecimal.ZERO);
//
//        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext, 0.003);
//
//        fakeTinkoffService.addInvestment(accountId, dateTime, currency, initialInvestment);
//
//        final WithdrawLimits withdrawLimits = fakeTinkoffService.getWithdrawLimits(accountId);
//
//        final BigDecimal balance = DataStructsHelper.getBalance(withdrawLimits.getMoney(), currency);
//        final BigDecimal blockedBalance = DataStructsHelper.getBalance(withdrawLimits.getBlocked(), currency);
//        final BigDecimal blockedGuaranteeBalance = DataStructsHelper.getBalance(withdrawLimits.getBlockedGuarantee(), currency);
//
//        AssertUtils.assertEquals(initialInvestment, balance);
//        AssertUtils.assertEquals(0, blockedBalance);
//        AssertUtils.assertEquals(0, blockedGuaranteeBalance);
//    }

    // endregion

//    private void postOrder(
//            final String accountId,
//            final String ticker,
//            final int quantityLots,
//            final OrderDirection direction,
//            final double price
//    ) throws IOException {
//        final Candle candle = new Candle().setClosePrice(DecimalUtils.setDefaultScale(price));
//        Mockito.when(extMarketDataService.getLastCandle(ticker, fakeTinkoffService.getCurrentDateTime())).thenReturn(candle);
//
//        fakeTinkoffService.postOrder(accountId, ticker, quantityLots, null, direction, OrderType.ORDER_TYPE_MARKET, null);
//    }

    @Test
    void getCurrentPrice() {
        final String ticker = "ticker";
        final BigDecimal price = BigDecimal.valueOf(100);
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 18, 59, 59);
        final FakeContext fakeContext = new FakeContext(dateTime, "2000124699", Currency.USD, BigDecimal.ZERO);

        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext, 0.003);

        Mockito.when(extMarketDataService.getLastCandle(ticker, fakeTinkoffService.getCurrentDateTime()))
                .thenReturn(new Candle().setClosePrice(price));

        final BigDecimal currentPrice = fakeTinkoffService.getCurrentPrice(ticker);

        AssertUtils.assertEquals(price, currentPrice);
    }

}