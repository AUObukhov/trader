package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.matchers.BigDecimalMatcher;
import ru.obukhov.trader.test.utils.matchers.PositionMatcher;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class FakeExtOrdersServiceUnitTest {

    private static final double COMMISSION = 0.003;
    private static final double COMMISSION_COEF = 1 + COMMISSION;

    @Mock
    private FakeContext fakeContext;
    @Mock
    private ExtInstrumentsService extInstrumentsService;
    @Mock
    private ExtMarketDataService extMarketDataService;

    private FakeExtOrdersService fakeExtOrdersService;

    @BeforeEach
    void setUp() {
        final BigDecimal decimalCommission = DecimalUtils.setDefaultScale(COMMISSION);
        fakeExtOrdersService = new FakeExtOrdersService(fakeContext, extInstrumentsService, extMarketDataService, decimalCommission);
    }

    // region getOrders tests

    @Test
    void getOrders_byAccountId_returnsEmptyList() {
        final String accountId = TestAccounts.TINKOFF.account().id();

        List<OrderState> orders = fakeExtOrdersService.getOrders(accountId);

        Assertions.assertTrue(orders.isEmpty());
    }

    @Test
    void getOrders_byAccountIdAndFigi_returnsEmptyList() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        final String figi = TestShares.APPLE.share().figi();

        List<OrderState> orders = fakeExtOrdersService.getOrders(accountId, figi);

        Assertions.assertTrue(orders.isEmpty());
    }

    // endregion

    // region postOrder tests. Implicit tests for getPortfolioPositions

    @Test
    void postOrder_buy_throwsIllegalArgumentException_whenNotEnoughBalance() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        final Share share = TestShares.APPLE.share();
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);

        mockBalances(accountId, share.currency(), 1000);
        Mocker.mockShare(extInstrumentsService, share);
        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(dateTime);

        final Executable executable = () -> postOrder(accountId, share.figi(), 2, OrderDirection.ORDER_DIRECTION_BUY, dateTime, 500);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "balance can't be negative");

        Mockito.verify(fakeContext, Mockito.never())
                .setBalance(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void postOrder_buy_createsNewPosition_whenNoPositions() {
        // arrange

        final String accountId = TestAccounts.TINKOFF.account().id();
        final Share share = TestShares.SBER.share();
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final String currency = share.currency();
        final String figi = share.figi();
        final int quantity = 10;
        final double currentPrice = 1000;
        final double balance1 = 1000000;
        final double balance2 = balance1 - currentPrice * quantity * COMMISSION_COEF;

        mockBalances(accountId, currency, balance1);
        Mockito.when(fakeContext.getPosition(accountId, figi)).thenReturn(null);
        Mocker.mockShare(extInstrumentsService, share);
        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(dateTime);

        // action

        postOrder(accountId, figi, quantity, OrderDirection.ORDER_DIRECTION_BUY, dateTime, currentPrice);

        // assert

        verifyBalanceSet(accountId, currency, balance2);
        final Position expectedPosition = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity)
                .setAveragePositionPrice(currentPrice)
                .setExpectedYield(0)
                .setCurrentPrice(currentPrice)
                .build();
        verifyPositionAdded(accountId, figi, expectedPosition);
    }

    @Test
    void postOrder_buy_addsValueToExistingPosition_whenPositionAlreadyExists() {
        // arrange

        final String accountId = TestAccounts.TINKOFF.account().id();
        final Share share = TestShares.SBER.share();
        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(5);
        final String currency = share.currency();
        final String figi = share.figi();
        final int quantity1 = 20;
        final int quantity2 = 10;
        final double price1 = 1000;
        final double price2 = 4000;
        final double balance1 = 1000000;
        final double balance2 = balance1 - price1 * quantity1 * COMMISSION_COEF;
        final double balance3 = balance2 - price2 * quantity2 * COMMISSION_COEF;

        mockBalances(accountId, currency, balance1, balance2);
        Mocker.mockShare(extInstrumentsService, share);
        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(dateTime1, dateTime1, dateTime2, dateTime2);

        final Position expectedPosition1 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity1)
                .setAveragePositionPrice(price1)
                .setExpectedYield(0)
                .setCurrentPrice(price1)
                .build();
        final Position expectedPosition2 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity1 + quantity2)
                .setAveragePositionPrice(getAveragePositionPrice(price1, price2, quantity1, quantity2))
                .setExpectedYield(quantity1 * (price2 - price1))
                .setCurrentPrice(price2)
                .build();

        Mockito.when(fakeContext.getPosition(accountId, figi)).thenReturn(null, expectedPosition1);

        // action

        postOrder(accountId, figi, quantity1, OrderDirection.ORDER_DIRECTION_BUY, dateTime1, price1);
        postOrder(accountId, figi, quantity2, OrderDirection.ORDER_DIRECTION_BUY, dateTime2, price2);

        // assert

        verifyBalanceSet(accountId, currency, balance2);
        verifyBalanceSet(accountId, currency, balance3);
        verifyPositionAdded(accountId, figi, expectedPosition1);
        verifyPositionAdded(accountId, figi, expectedPosition2);
    }

    @Test
    void postOrder_buy_createsMultiplePositions_whenDifferentFigiesAreBought() {
        // arrange

        final String accountId = TestAccounts.TINKOFF.account().id();

        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(5);
        final OffsetDateTime dateTime3 = dateTime1.plusMinutes(10);

        final Share share1 = TestShares.SBER.share();
        final Share share2 = TestShares.YANDEX.share();
        final Share share3 = TestShares.DIOD.share();

        final String currency = share1.currency();
        final double initialBalance = 1000000;
        final double balance1 = 989970;
        final double balance2 = 989669.1;
        final double balance3 = 939519.1;

        final String figi1 = share1.figi();
        final String figi2 = share2.figi();
        final String figi3 = share3.figi();

        final double price1 = 1000;
        final double price2 = 100;
        final double price3 = 500;

        final int quantity1 = 10;
        final int quantity2 = 3;
        final int quantity3 = 100;

        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(dateTime1, dateTime1, dateTime2, dateTime2, dateTime3, dateTime3);
        mockBalances(accountId, currency, initialBalance, balance1, balance2);

        Mocker.mockShare(extInstrumentsService, share1);
        Mocker.mockShare(extInstrumentsService, share2);
        Mocker.mockShare(extInstrumentsService, share3);

        final Position expectedPosition1 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi1)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity1)
                .setAveragePositionPrice(price1)
                .setExpectedYield(0)
                .setCurrentPrice(price1)
                .build();

        final Position expectedPosition2 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi2)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity2)
                .setAveragePositionPrice(price2)
                .setExpectedYield(0)
                .setCurrentPrice(price2)
                .build();

        final Position expectedPosition3 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi3)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity3)
                .setAveragePositionPrice(price3)
                .setExpectedYield(0)
                .setCurrentPrice(price3)
                .build();

        Mockito.when(fakeContext.getPosition(accountId, figi1)).thenReturn(null, expectedPosition1);
        Mockito.when(fakeContext.getPosition(accountId, figi2)).thenReturn(null, expectedPosition2);
        Mockito.when(fakeContext.getPosition(accountId, figi3)).thenReturn(null, expectedPosition3);

        // action

        postOrder(accountId, figi1, quantity1, OrderDirection.ORDER_DIRECTION_BUY, dateTime1, price1);
        postOrder(accountId, figi2, quantity2, OrderDirection.ORDER_DIRECTION_BUY, dateTime2, price2);
        postOrder(accountId, figi3, quantity3, OrderDirection.ORDER_DIRECTION_BUY, dateTime3, price3);

        // assert

        verifyPositionAdded(accountId, figi1, expectedPosition1);
        verifyPositionAdded(accountId, figi2, expectedPosition2);
        verifyPositionAdded(accountId, figi3, expectedPosition3);

        verifyBalanceSet(accountId, currency, balance1);
        verifyBalanceSet(accountId, currency, balance2);
        verifyBalanceSet(accountId, currency, balance3);
    }

    @Test
    void postOrder_sell_throwsIllegalArgumentException_whenSellsMoreLotsThanExists() {
        // arrange

        final String accountId = TestAccounts.TINKOFF.account().id();
        final Share share = TestShares.SBER.share();

        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(5);
        final OffsetDateTime dateTime3 = dateTime1.plusMinutes(10);

        final String currency = share.currency();

        final double initialBalance = 1000000;
        final double balance1 = 979940;
        final double balance2 = 939820;

        final String figi = share.figi();

        final double price1 = 1000;
        final double price2 = 4000;
        final double price3 = 3000;

        final int quantity1 = 20;
        final int quantity2 = 10;
        final int quantity3 = 40;

        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(dateTime1, dateTime1, dateTime2, dateTime2, dateTime3, dateTime3);
        mockBalances(accountId, currency, initialBalance, balance1, balance2);
        Mocker.mockShare(extInstrumentsService, share);

        final Position expectedPosition1 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity1)
                .setAveragePositionPrice(price1)
                .setExpectedYield(0)
                .setCurrentPrice(price1)
                .build();
        final Position expectedPosition2 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity1 + quantity2)
                .setAveragePositionPrice(getAveragePositionPrice(price1, price2, quantity1, quantity2))
                .setExpectedYield(quantity1 * (price2 - price1))
                .setCurrentPrice(price2)
                .build();

        Mockito.when(fakeContext.getPosition(accountId, figi)).thenReturn(null, expectedPosition1, expectedPosition2);

        // action & assert

        postOrder(accountId, figi, quantity1, OrderDirection.ORDER_DIRECTION_BUY, dateTime1, price1);
        postOrder(accountId, figi, quantity2, OrderDirection.ORDER_DIRECTION_BUY, dateTime2, price2);
        final Executable sellExecutable = () -> postOrder(accountId, figi, quantity3, OrderDirection.ORDER_DIRECTION_SELL, dateTime3, price3);
        final String expectedMessage = "quantity 40 can't be greater than existing position's quantity 30";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, sellExecutable, expectedMessage);

        verifyPositionAdded(accountId, figi, expectedPosition1);
        verifyPositionAdded(accountId, figi, expectedPosition2);

        verifyBalanceSet(accountId, currency, balance1);
        verifyBalanceSet(accountId, currency, balance2);
    }

    @Test
    void postOrder_sell_removesPosition_whenAllLotsAreSold() {
        // arrange

        final String accountId = TestAccounts.TINKOFF.account().id();
        final Share share = TestShares.SBER.share();

        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(5);
        final OffsetDateTime dateTime3 = dateTime1.plusMinutes(10);

        final String currency = share.currency();

        final double initialBalance = 1000000;
        final double balance1 = 979940;
        final double balance2 = 939820;
        final double balance3 = 1029550;

        final String figi = share.figi();

        final double price1 = 1000;
        final double price2 = 4000;
        final double price3 = 3000;

        final int quantity1 = 20;
        final int quantity2 = 10;
        final int quantity3 = 30;

        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(dateTime1, dateTime1, dateTime2, dateTime2, dateTime3, dateTime3);
        mockBalances(accountId, currency, initialBalance, balance1, balance2);
        Mocker.mockShare(extInstrumentsService, share);

        final Position expectedPosition1 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity1)
                .setAveragePositionPrice(price1)
                .setExpectedYield(0)
                .setCurrentPrice(price1)
                .build();
        final Position expectedPosition2 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity1 + quantity2)
                .setAveragePositionPrice(getAveragePositionPrice(price1, price2, quantity1, quantity2))
                .setExpectedYield(quantity1 * (price2 - price1))
                .setCurrentPrice(price2)
                .build();
        Mockito.when(fakeContext.getPosition(accountId, figi)).thenReturn(null, expectedPosition1, expectedPosition2);

        // action

        postOrder(accountId, figi, quantity1, OrderDirection.ORDER_DIRECTION_BUY, dateTime1, price1);
        postOrder(accountId, figi, quantity2, OrderDirection.ORDER_DIRECTION_BUY, dateTime2, price2);
        postOrder(accountId, figi, quantity3, OrderDirection.ORDER_DIRECTION_SELL, dateTime3, price3);

        // assert

        verifyPositionAdded(accountId, figi, expectedPosition1);
        verifyPositionAdded(accountId, figi, expectedPosition2);
        Mockito.verify(fakeContext, Mockito.times(1)).removePosition(accountId, figi);

        verifyBalanceSet(accountId, currency, balance1);
        verifyBalanceSet(accountId, currency, balance2);
        verifyBalanceSet(accountId, currency, balance3);
    }

    @Test
    void postOrder_sell_reducesLotsCount() {
        // arrange

        final String accountId = TestAccounts.TINKOFF.account().id();
        final Share share = TestShares.SBER.share();

        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(5);
        final OffsetDateTime dateTime3 = dateTime1.plusMinutes(10);

        final String currency = share.currency();

        final double initialBalance = 1000000;
        final double balance1 = 979940;
        final double balance2 = 939820;
        final double balance3 = 969730;

        final String figi = share.figi();

        final double price1 = 1000;
        final double price2 = 4000;
        final double price3 = 3000;

        final int quantity1 = 20;
        final int quantity2 = 10;
        final int quantity3 = 10;

        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(dateTime1, dateTime1, dateTime2, dateTime2, dateTime3, dateTime3);
        mockBalances(accountId, currency, initialBalance, balance1, balance2);
        Mocker.mockShare(extInstrumentsService, share);

        final Position expectedPosition1 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(quantity1)
                .setAveragePositionPrice(price1)
                .setExpectedYield(0)
                .setCurrentPrice(price1)
                .build();
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
        final int finalQuantity = quantity1 + quantity2 - quantity3;
        final double expectedYield3 = (price3 - expectedAveragePositionPrice2) * (finalQuantity);
        final Position expectedPosition3 = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(finalQuantity)
                .setAveragePositionPrice(expectedAveragePositionPrice2)
                .setExpectedYield(expectedYield3)
                .setCurrentPrice(price3)
                .build();

        Mockito.when(fakeContext.getPosition(accountId, figi)).thenReturn(null, expectedPosition1, expectedPosition2, expectedPosition3);

        // action

        postOrder(accountId, figi, quantity1, OrderDirection.ORDER_DIRECTION_BUY, dateTime1, price1);
        postOrder(accountId, figi, quantity2, OrderDirection.ORDER_DIRECTION_BUY, dateTime2, price2);
        postOrder(accountId, figi, quantity3, OrderDirection.ORDER_DIRECTION_SELL, dateTime3, price3);

        // assert

        verifyPositionAdded(accountId, figi, expectedPosition1);
        verifyPositionAdded(accountId, figi, expectedPosition2);
        verifyPositionAdded(accountId, figi, expectedPosition3);

        verifyBalanceSet(accountId, currency, balance1);
        verifyBalanceSet(accountId, currency, balance2);
        verifyBalanceSet(accountId, currency, balance3);
    }

    @SuppressWarnings("SameParameterValue")
    private void mockBalances(final String accountId, final String currency, double balance1, double... balances) {
        final BigDecimal[] decimalBalances = Arrays.stream(balances)
                .mapToObj(DecimalUtils::setDefaultScale)
                .toArray(BigDecimal[]::new);
        Mockito.when(fakeContext.getBalance(accountId, currency)).thenReturn(DecimalUtils.setDefaultScale(balance1), decimalBalances);
    }

    private static double getAveragePositionPrice(
            final double price1,
            final double price2,
            final int quantity1,
            final int quantity2
    ) {
        return (price1 * quantity1 + price2 * quantity2) / (quantity1 + quantity2);
    }

    @SuppressWarnings("SameParameterValue")
    private void postOrder(
            final String accountId,
            final String figi,
            final int quantity,
            final OrderDirection direction,
            final OffsetDateTime dateTime,
            final double price
    ) {
        Mockito.when(extMarketDataService.getLastPrice(figi, dateTime))
                .thenReturn(DecimalUtils.setDefaultScale(price));

        fakeExtOrdersService.postOrder(accountId, figi, quantity, null, direction, OrderType.ORDER_TYPE_MARKET, null);
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyBalanceSet(final String accountId, final String currency, final double balance) {
        Mockito.verify(fakeContext, Mockito.times(1))
                .setBalance(Mockito.eq(accountId), Mockito.eq(currency), ArgumentMatchers.argThat(BigDecimalMatcher.of(balance)));
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyPositionAdded(final String accountId, final String figi, final Position position) {
        Mockito.verify(fakeContext, Mockito.times(1))
                .addPosition(Mockito.eq(accountId), Mockito.eq(figi), ArgumentMatchers.argThat(PositionMatcher.of(position)));
    }

    // endregion

    @Test
    void cancelOrder_throwsUnsupportedOperationException() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        final String orderId = "orderId";

        AssertUtils.assertThrowsWithMessage(
                UnsupportedOperationException.class,
                () -> fakeExtOrdersService.cancelOrder(accountId, orderId),
                "Back test does not support cancelling of orders"
        );
    }

}