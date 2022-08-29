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

        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext);

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

        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext);

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

        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext);

        final OffsetDateTime nextMinuteDateTime = fakeTinkoffService.nextMinute();

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(3), MARKET_PROPERTIES.getWorkSchedule().getStartTime());
        Assertions.assertEquals(expected, nextMinuteDateTime);
        Assertions.assertEquals(expected, fakeTinkoffService.getCurrentDateTime());
    }

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

        fakeTinkoffService = new FakeTinkoffService(MARKET_PROPERTIES, tinkoffServices, fakeContext);

        Mockito.when(extMarketDataService.getLastCandle(ticker, fakeTinkoffService.getCurrentDateTime()))
                .thenReturn(new Candle().setClosePrice(price));

        final BigDecimal currentPrice = fakeTinkoffService.getCurrentPrice(ticker);

        AssertUtils.assertEquals(price, currentPrice);
    }

}