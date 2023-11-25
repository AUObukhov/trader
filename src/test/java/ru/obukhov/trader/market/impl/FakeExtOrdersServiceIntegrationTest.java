package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.CandleMocker;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@SpringBootTest
class FakeExtOrdersServiceIntegrationTest extends IntegrationTest {

    private static final double COMMISSION = 0.003;
    private static final double COMMISSION_COEF = 1 + COMMISSION;

    @Autowired
    private ExtInstrumentsService extInstrumentsService;
    @Autowired
    private ExtMarketDataService extMarketDataService;

    private FakeExtOrdersService fakeExtOrdersService;

    // region getOrders tests

    @Test
    void getOrders_byAccountId_returnsEmptyList() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        initializeContextAndService(OffsetDateTime.now());

        List<OrderState> orders = fakeExtOrdersService.getOrders(accountId);

        Assertions.assertTrue(orders.isEmpty());
    }

    @Test
    void getOrders_byAccountIdAndFigi_returnsEmptyList() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        final String figi = TestShares.APPLE.share().figi();
        initializeContextAndService(OffsetDateTime.now());

        List<OrderState> orders = fakeExtOrdersService.getOrders(accountId, figi);

        Assertions.assertTrue(orders.isEmpty());
    }

    // endregion

    // region postOrder tests. Implicit tests for getPortfolioPositions

    @Test
    void postOrder_buy_throwsIllegalArgumentException_whenNotEnoughBalance() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        final ru.tinkoff.piapi.contract.v1.Share share = TestShares.APPLE.tinkoffShare();
        final OffsetDateTime dateTime = DateTimeTestData.newDateTime(2020, 10, 5, 12);
        final int initialBalance = 1000;

        Mocker.mockShare(instrumentsService, share);
        new CandleMocker(marketDataService, share.getFigi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(1000, dateTime.minusMinutes(1))
                .mock();

        final FakeContext fakeContext = initializeContextAndService(accountId, share.getCurrency(), dateTime, initialBalance);

        final Executable executable =
                () -> postOrder(fakeContext, accountId, share.getFigi(), 2, OrderDirection.ORDER_DIRECTION_BUY, dateTime);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "balance can't be negative");

        AssertUtils.assertEquals(initialBalance, fakeContext.getBalance(accountId, share.getCurrency()));
    }

    @Test
    void postOrder_buy_createsNewPosition_whenNoPositions() {
        // arrange

        final String accountId = TestAccounts.TINKOFF.account().id();

        final ru.tinkoff.piapi.contract.v1.Share share = TestShares.SBER.tinkoffShare();
        final String figi = share.getFigi();
        final String currency = share.getCurrency();

        final OffsetDateTime dateTime = DateTimeTestData.newDateTime(2020, 10, 5, 12);
        final int quantity = 10;
        final int currentPrice = 1000;
        final int balance1 = 1000000;
        final double balance2 = balance1 - currentPrice * quantity * COMMISSION_COEF;

        Mocker.mockShare(instrumentsService, share);
        new CandleMocker(marketDataService, share.getFigi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(1000, dateTime.minusMinutes(1))
                .mock();

        final FakeContext fakeContext = initializeContextAndService(accountId, share.getCurrency(), dateTime, balance1);

        // act

        postOrder(fakeContext, accountId, figi, quantity, OrderDirection.ORDER_DIRECTION_BUY, dateTime);

        // assert

        AssertUtils.assertEquals(balance2, fakeContext.getBalance(accountId, currency));
        final Position expectedPosition = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity)
                .setAveragePositionPrice(currentPrice)
                .setExpectedYield(0)
                .setCurrentPrice(currentPrice)
                .build();
        Assertions.assertEquals(expectedPosition, fakeContext.getPosition(accountId, figi));
    }

    @Test
    void postOrder_buy_addsValueToExistingPosition_whenPositionAlreadyExists() {
        // arrange

        final String accountId = TestAccounts.TINKOFF.account().id();

        final ru.tinkoff.piapi.contract.v1.Share share = TestShares.SBER.tinkoffShare();
        final String figi = share.getFigi();
        final String currency = share.getCurrency();

        final OffsetDateTime dateTime1 = DateTimeTestData.newDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(5);

        final int quantity1 = 20;
        final int quantity2 = 10;

        final int price1 = 1000;
        final int price2 = 4000;

        final int initialBalance = 1000000;
        final double balance1 = initialBalance - price1 * quantity1 * COMMISSION_COEF;
        final double balance2 = balance1 - price2 * quantity2 * COMMISSION_COEF;

        Mocker.mockShare(instrumentsService, share);
        new CandleMocker(marketDataService, share.getFigi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(price1, dateTime1.minusMinutes(1))
                .add(price2, dateTime2.minusMinutes(1))
                .mock();

        final FakeContext fakeContext = initializeContextAndService(accountId, share.getCurrency(), dateTime1, initialBalance);

        // act

        postOrder(fakeContext, accountId, figi, quantity1, OrderDirection.ORDER_DIRECTION_BUY, dateTime1);
        final BigDecimal actualBalance1 = fakeContext.getBalance(accountId, currency);
        final Position actualPosition1 = fakeContext.getPosition(accountId, figi);

        postOrder(fakeContext, accountId, figi, quantity2, OrderDirection.ORDER_DIRECTION_BUY, dateTime2);
        final BigDecimal actualBalance2 = fakeContext.getBalance(accountId, currency);
        final Position actualPosition2 = fakeContext.getPosition(accountId, figi);

        // assert

        AssertUtils.assertEquals(balance1, actualBalance1);
        final Position expectedPosition1 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity1)
                .setAveragePositionPrice(price1)
                .setExpectedYield(0)
                .setCurrentPrice(price1)
                .build();
        Assertions.assertEquals(expectedPosition1, actualPosition1);

        AssertUtils.assertEquals(balance2, actualBalance2);
        final Position expectedPosition2 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity1 + quantity2)
                .setAveragePositionPrice(getAveragePositionPrice(price1, price2, quantity1, quantity2))
                .setExpectedYield(quantity1 * (price2 - price1))
                .setCurrentPrice(price2)
                .build();
        Assertions.assertEquals(expectedPosition2, actualPosition2);
    }

    @Test
    void postOrder_buy_createsMultiplePositions_whenDifferentFigiesAreBought() {
        // arrange

        final String accountId = TestAccounts.TINKOFF.account().id();

        final ru.tinkoff.piapi.contract.v1.Share share1 = TestShares.SBER.tinkoffShare();
        final ru.tinkoff.piapi.contract.v1.Share share2 = TestShares.YANDEX.tinkoffShare();
        final ru.tinkoff.piapi.contract.v1.Share share3 = TestShares.DIOD.tinkoffShare();

        final String figi1 = share1.getFigi();
        final String figi2 = share2.getFigi();
        final String figi3 = share3.getFigi();

        final String currency = share1.getCurrency();

        final OffsetDateTime dateTime1 = DateTimeTestData.newDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(5);
        final OffsetDateTime dateTime3 = dateTime1.plusMinutes(10);

        final int initialBalance = 1000000;

        final int price1 = 1000;
        final int price2 = 100;
        final int price3 = 500;

        final int quantity1 = 10;
        final int quantity2 = 3;
        final int quantity3 = 100;

        Mocker.mockShare(instrumentsService, share1);
        Mocker.mockShare(instrumentsService, share2);
        Mocker.mockShare(instrumentsService, share3);

        new CandleMocker(marketDataService, share1.getFigi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(price1, dateTime1.minusMinutes(1))
                .mock();
        new CandleMocker(marketDataService, share2.getFigi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(price2, dateTime2.minusMinutes(1))
                .mock();
        new CandleMocker(marketDataService, share3.getFigi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(price3, dateTime3.minusMinutes(1))
                .mock();

        final FakeContext fakeContext = initializeContextAndService(accountId, currency, dateTime1, initialBalance);

        // act

        postOrder(fakeContext, accountId, figi1, quantity1, OrderDirection.ORDER_DIRECTION_BUY, dateTime1);
        final BigDecimal actualBalance1 = fakeContext.getBalance(accountId, currency);

        postOrder(fakeContext, accountId, figi2, quantity2, OrderDirection.ORDER_DIRECTION_BUY, dateTime2);
        final BigDecimal actualBalance2 = fakeContext.getBalance(accountId, currency);

        postOrder(fakeContext, accountId, figi3, quantity3, OrderDirection.ORDER_DIRECTION_BUY, dateTime3);
        final BigDecimal actualBalance3 = fakeContext.getBalance(accountId, currency);

        // assert

        AssertUtils.assertEquals(989970, actualBalance1);
        final Position expectedPosition1 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi1)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity1)
                .setAveragePositionPrice(price1)
                .setExpectedYield(0)
                .setCurrentPrice(price1)
                .build();
        Assertions.assertEquals(expectedPosition1, fakeContext.getPosition(accountId, figi1));

        AssertUtils.assertEquals(989669.1, actualBalance2);
        final Position expectedPosition2 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi2)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity2)
                .setAveragePositionPrice(price2)
                .setExpectedYield(0)
                .setCurrentPrice(price2)
                .build();
        Assertions.assertEquals(expectedPosition2, fakeContext.getPosition(accountId, figi2));

        AssertUtils.assertEquals(939519.1, actualBalance3);
        final Position expectedPosition3 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi3)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity3)
                .setAveragePositionPrice(price3)
                .setExpectedYield(0)
                .setCurrentPrice(price3)
                .build();
        Assertions.assertEquals(expectedPosition3, fakeContext.getPosition(accountId, figi3));
    }

    @Test
    void postOrder_sell_throwsIllegalArgumentException_whenSellsMoreLotsThanExists() {
        // arrange

        final String accountId = TestAccounts.TINKOFF.account().id();

        final ru.tinkoff.piapi.contract.v1.Share share = TestShares.SBER.tinkoffShare();
        final String figi = share.getFigi();
        final String currency = share.getCurrency();

        final OffsetDateTime dateTime1 = DateTimeTestData.newDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(5);
        final OffsetDateTime dateTime3 = dateTime1.plusMinutes(10);

        final int initialBalance = 1000000;

        final int price1 = 1000;
        final int price2 = 4000;
        final int price3 = 3000;

        final int quantity1 = 20;
        final int quantity2 = 10;
        final int quantity3 = 40;

        Mocker.mockShare(instrumentsService, share);
        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(price1, dateTime1.minusMinutes(1))
                .add(price2, dateTime2.minusMinutes(1))
                .add(price3, dateTime3.minusMinutes(1))
                .mock();

        final FakeContext fakeContext = initializeContextAndService(accountId, currency, dateTime1, initialBalance);

        // act

        postOrder(fakeContext, accountId, figi, quantity1, OrderDirection.ORDER_DIRECTION_BUY, dateTime1);
        final Position actualPosition1 = fakeContext.getPosition(accountId, figi);
        final BigDecimal actualBalance1 = fakeContext.getBalance(accountId, currency);

        postOrder(fakeContext, accountId, figi, quantity2, OrderDirection.ORDER_DIRECTION_BUY, dateTime2);
        final Position actualPosition2 = fakeContext.getPosition(accountId, figi);
        final BigDecimal actualBalance2 = fakeContext.getBalance(accountId, currency);

        final Executable sellExecutable = () -> postOrder(fakeContext, accountId, figi, quantity3, OrderDirection.ORDER_DIRECTION_SELL, dateTime3);
        final String expectedMessage = "quantity 40 can't be greater than existing position's quantity 30";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, sellExecutable, expectedMessage);

        // assert

        AssertUtils.assertEquals(979940, actualBalance1);
        final Position expectedPosition1 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity1)
                .setAveragePositionPrice(price1)
                .setExpectedYield(0)
                .setCurrentPrice(price1)
                .build();
        Assertions.assertEquals(expectedPosition1, actualPosition1);

        AssertUtils.assertEquals(939820, actualBalance2);
        final Position expectedPosition2 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity1 + quantity2)
                .setAveragePositionPrice(getAveragePositionPrice(price1, price2, quantity1, quantity2))
                .setExpectedYield(quantity1 * (price2 - price1))
                .setCurrentPrice(price2)
                .build();
        Assertions.assertEquals(expectedPosition2, actualPosition2);
    }

    @Test
    void postOrder_sell_removesPosition_whenAllLotsAreSold() {
        // arrange

        final String accountId = TestAccounts.TINKOFF.account().id();

        final ru.tinkoff.piapi.contract.v1.Share share = TestShares.SBER.tinkoffShare();
        final String figi = share.getFigi();
        final String currency = share.getCurrency();

        final OffsetDateTime dateTime1 = DateTimeTestData.newDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(5);
        final OffsetDateTime dateTime3 = dateTime1.plusMinutes(10);

        final int initialBalance = 1000000;
        final int balance1 = 979940;
        final int balance2 = 939820;
        final int balance3 = 1029550;

        final int price1 = 1000;
        final int price2 = 4000;
        final int price3 = 3000;

        final int quantity1 = 20;
        final int quantity2 = 10;
        final int quantity3 = 30;

        Mocker.mockShare(instrumentsService, share);
        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(price1, dateTime1.minusMinutes(1))
                .add(price2, dateTime2.minusMinutes(1))
                .add(price3, dateTime3.minusMinutes(1))
                .mock();

        final FakeContext fakeContext = initializeContextAndService(accountId, currency, dateTime1, initialBalance);

        // act

        postOrder(fakeContext, accountId, figi, quantity1, OrderDirection.ORDER_DIRECTION_BUY, dateTime1);
        final Position actualPosition1 = fakeContext.getPosition(accountId, figi);
        final BigDecimal actualBalance1 = fakeContext.getBalance(accountId, currency);

        postOrder(fakeContext, accountId, figi, quantity2, OrderDirection.ORDER_DIRECTION_BUY, dateTime2);
        final Position actualPosition2 = fakeContext.getPosition(accountId, figi);
        final BigDecimal actualBalance2 = fakeContext.getBalance(accountId, currency);

        postOrder(fakeContext, accountId, figi, quantity3, OrderDirection.ORDER_DIRECTION_SELL, dateTime3);
        final Position actualPosition3 = fakeContext.getPosition(accountId, figi);
        final BigDecimal actualBalance3 = fakeContext.getBalance(accountId, currency);

        // assert

        AssertUtils.assertEquals(balance1, actualBalance1);
        final Position expectedPosition1 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity1)
                .setAveragePositionPrice(price1)
                .setExpectedYield(0)
                .setCurrentPrice(price1)
                .build();
        Assertions.assertEquals(expectedPosition1, actualPosition1);

        final Position expectedPosition2 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity1 + quantity2)
                .setAveragePositionPrice(getAveragePositionPrice(price1, price2, quantity1, quantity2))
                .setExpectedYield(quantity1 * (price2 - price1))
                .setCurrentPrice(price2)
                .build();
        Assertions.assertEquals(expectedPosition2, actualPosition2);
        AssertUtils.assertEquals(balance2, actualBalance2);

        AssertUtils.assertEquals(balance3, actualBalance3);
        Assertions.assertNull(actualPosition3);
    }

    @Test
    void postOrder_sell_reducesLotsCount() {
        // arrange

        final String accountId = TestAccounts.TINKOFF.account().id();

        final ru.tinkoff.piapi.contract.v1.Share share = TestShares.SBER.tinkoffShare();
        final String figi = share.getFigi();
        final String currency = share.getCurrency();

        final OffsetDateTime dateTime1 = DateTimeTestData.newDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(5);
        final OffsetDateTime dateTime3 = dateTime1.plusMinutes(10);

        final int initialBalance = 1000000;
        final int balance1 = 979940;
        final int balance2 = 939820;
        final int balance3 = 969730;

        final int price1 = 1000;
        final int price2 = 4000;
        final int price3 = 3000;

        final int quantity1 = 20;
        final int quantity2 = 10;
        final int quantity3 = 10;

        Mocker.mockShare(instrumentsService, share);
        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(price1, dateTime1.minusMinutes(1))
                .add(price2, dateTime2.minusMinutes(1))
                .add(price3, dateTime3.minusMinutes(1))
                .mock();

        final FakeContext fakeContext = initializeContextAndService(accountId, currency, dateTime1, initialBalance);

        // act

        postOrder(fakeContext, accountId, figi, quantity1, OrderDirection.ORDER_DIRECTION_BUY, dateTime1);
        final Position actualPosition1 = fakeContext.getPosition(accountId, figi);
        final BigDecimal actualBalance1 = fakeContext.getBalance(accountId, currency);

        postOrder(fakeContext, accountId, figi, quantity2, OrderDirection.ORDER_DIRECTION_BUY, dateTime2);
        final Position actualPosition2 = fakeContext.getPosition(accountId, figi);
        final BigDecimal actualBalance2 = fakeContext.getBalance(accountId, currency);

        postOrder(fakeContext, accountId, figi, quantity3, OrderDirection.ORDER_DIRECTION_SELL, dateTime3);
        final Position actualPosition3 = fakeContext.getPosition(accountId, figi);
        final BigDecimal actualBalance3 = fakeContext.getBalance(accountId, currency);

        // assert

        final Position expectedPosition1 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity1)
                .setAveragePositionPrice(price1)
                .setExpectedYield(0)
                .setCurrentPrice(price1)
                .build();
        Assertions.assertEquals(expectedPosition1, actualPosition1);
        AssertUtils.assertEquals(balance1, actualBalance1);

        final double expectedAveragePositionPrice2 = getAveragePositionPrice(price1, price2, quantity1, quantity2);
        final Position expectedPosition2 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity1 + quantity2)
                .setAveragePositionPrice(expectedAveragePositionPrice2)
                .setExpectedYield(quantity1 * (price2 - price1))
                .setCurrentPrice(price2)
                .build();
        Assertions.assertEquals(expectedPosition2, actualPosition2);
        AssertUtils.assertEquals(balance2, actualBalance2);

        final int expectedQuantity3 = quantity1 + quantity2 - quantity3;
        final double expectedYield3 = (price3 - expectedAveragePositionPrice2) * (expectedQuantity3);
        final Position expectedPosition3 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(expectedQuantity3)
                .setAveragePositionPrice(expectedAveragePositionPrice2)
                .setExpectedYield(expectedYield3)
                .setCurrentPrice(price3)
                .build();
        Assertions.assertEquals(expectedPosition3, actualPosition3);
        AssertUtils.assertEquals(balance3, actualBalance3);
    }

    @SuppressWarnings("SameParameterValue")
    private static double getAveragePositionPrice(final int price1, final int price2, final int quantity1, final int quantity2) {
        return ((double) (price1 * quantity1 + price2 * quantity2)) / (quantity1 + quantity2);
    }

    @SuppressWarnings("SameParameterValue")
    private void postOrder(
            final FakeContext fakeContext,
            final String accountId,
            final String figi,
            final int quantity,
            final OrderDirection direction,
            final OffsetDateTime dateTime
    ) {
        fakeContext.setCurrentDateTime(dateTime);

        fakeExtOrdersService.postOrder(accountId, figi, quantity, null, direction, OrderType.ORDER_TYPE_MARKET, null);
    }

    // endregion

    @Test
    void cancelOrder_throwsUnsupportedOperationException() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        final String orderId = "orderId";
        initializeContextAndService(OffsetDateTime.now());

        final Executable executable = () -> fakeExtOrdersService.cancelOrder(accountId, orderId);
        final String expectedMessage = "Back test does not support cancelling of orders";
        AssertUtils.assertThrowsWithMessage(UnsupportedOperationException.class, executable, expectedMessage);
    }

    private FakeContext initializeContextAndService(final String accountId, final String currency, final OffsetDateTime dateTime, final int balance) {
        final FakeContext fakeContext = initializeContextAndService(dateTime);
        fakeContext.setBalance(accountId, currency, DecimalUtils.setDefaultScale(balance));
        return fakeContext;
    }

    private FakeContext initializeContextAndService(final OffsetDateTime dateTime) {
        final FakeContext fakeContext = new FakeContext(dateTime);
        final BigDecimal decimalCommission = DecimalUtils.setDefaultScale(COMMISSION);
        fakeExtOrdersService = new FakeExtOrdersService(fakeContext, extInstrumentsService, extMarketDataService, decimalCommission);
        return fakeContext;
    }

}