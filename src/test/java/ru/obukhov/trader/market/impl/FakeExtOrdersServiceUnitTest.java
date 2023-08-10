package ru.obukhov.trader.market.impl;

import com.google.protobuf.Timestamp;
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
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.matchers.BigDecimalMatcher;
import ru.obukhov.trader.test.utils.matchers.PositionMatcher;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
import ru.obukhov.trader.test.utils.model.share.TestShare3;
import ru.obukhov.trader.test.utils.model.share.TestShare4;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderState;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
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
        fakeExtOrdersService = new FakeExtOrdersService(fakeContext, extInstrumentsService, extMarketDataService, COMMISSION);
    }

    // region getOrders tests

    @Test
    void getOrders_byAccountId_returnsEmptyList() {
        final String accountId = TestData.ACCOUNT_ID1;

        List<OrderState> orders = fakeExtOrdersService.getOrders(accountId);

        Assertions.assertTrue(orders.isEmpty());
    }

    @Test
    void getOrders_byAccountIdAndFigi_returnsEmptyList() {
        final String accountId = TestData.ACCOUNT_ID1;
        final String figi = TestShare1.FIGI;

        List<OrderState> orders = fakeExtOrdersService.getOrders(accountId, figi);

        Assertions.assertTrue(orders.isEmpty());
    }

    // endregion

    // region postOrder tests. Implicit tests for getPortfolioPositions

    @Test
    void postOrder_buy_throwsIllegalArgumentException_whenNotEnoughBalance() {
        final String accountId = TestData.ACCOUNT_ID1;
        final Timestamp timestamp = TimestampUtils.newTimestamp(2020, 10, 5, 12);

        Mockito.when(fakeContext.getCurrentTimestamp()).thenReturn(timestamp);
        mockBalances(accountId, TestShare1.CURRENCY, 1000);
        Mocker.mockShare(extInstrumentsService, TestShare1.SHARE);

        final Executable executable = () -> postOrder(accountId, TestShare1.FIGI, 2, OrderDirection.ORDER_DIRECTION_BUY, timestamp, 500);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "balance can't be negative");

        Mockito.verify(fakeContext, Mockito.never())
                .setBalance(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void postOrder_buy_createsNewPosition_whenNoPositions() {
        // arrange

        final String accountId = TestData.ACCOUNT_ID1;
        final Timestamp timestamp = TimestampUtils.newTimestamp(2020, 10, 5, 12);
        final String currency = TestShare2.CURRENCY;
        final String figi = TestShare2.FIGI;
        final int quantity = 10;
        final int quantityLots = 1;
        final double currentPrice = 1000;
        final double balance1 = 1000000;
        final double balance2 = balance1 - currentPrice * quantity * COMMISSION_COEF;

        Mockito.when(fakeContext.getCurrentTimestamp()).thenReturn(timestamp);
        mockBalances(accountId, currency, balance1);
        Mockito.when(fakeContext.getPosition(accountId, figi)).thenReturn(null);
        Mocker.mockShare(extInstrumentsService, TestShare2.SHARE);

        // action

        postOrder(accountId, figi, quantityLots, OrderDirection.ORDER_DIRECTION_BUY, timestamp, currentPrice);

        // assert

        verifyBalanceSet(accountId, currency, balance2);
        final Money price = TestData.createMoney(currency, currentPrice);
        final Position expectedPosition = Position.builder()
                .figi(figi)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(BigDecimal.valueOf(quantity))
                .averagePositionPrice(price)
                .expectedYield(DecimalUtils.setDefaultScale(0))
                .currentPrice(price)
                .quantityLots(BigDecimal.valueOf(quantityLots))
                .build();
        verifyPositionAdded(accountId, figi, expectedPosition);
    }

    @Test
    void postOrder_buy_addsValueToExistingPosition_whenPositionAlreadyExists() {
        // arrange

        final String accountId = TestData.ACCOUNT_ID1;
        final Timestamp timestamp1 = TimestampUtils.newTimestamp(2020, 10, 5, 12);
        final Timestamp timestamp2 = TimestampUtils.plusMinutes(timestamp1, 5);
        final String currency = TestShare2.CURRENCY;
        final String figi = TestShare2.FIGI;
        final int quantity1 = 20;
        final int quantity2 = 10;
        final int quantityLots1 = 2;
        final int quantityLots2 = 1;
        final double price1 = 1000;
        final double price2 = 4000;
        final double balance1 = 1000000;
        final double balance2 = balance1 - price1 * quantity1 * COMMISSION_COEF;
        final double balance3 = balance2 - price2 * quantity2 * COMMISSION_COEF;

        Mockito.when(fakeContext.getCurrentTimestamp()).thenReturn(timestamp1, timestamp1, timestamp2, timestamp2);
        mockBalances(accountId, currency, balance1, balance2);
        Mocker.mockShare(extInstrumentsService, TestShare2.SHARE);

        final Money price1Money = TestData.createMoney(currency, price1);
        final Position expectedPosition1 = Position.builder()
                .figi(figi)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(BigDecimal.valueOf(quantity1))
                .averagePositionPrice(price1Money)
                .expectedYield(BigDecimal.ZERO)
                .currentPrice(price1Money)
                .quantityLots(BigDecimal.valueOf(quantityLots1))
                .build();

        final BigDecimal expectedQuantity = BigDecimal.valueOf(quantity1 + quantity2);
        final Money expectedAveragePositionPrice = TestData.createMoney(
                currency,
                (price1 * quantityLots1 + price2 * quantityLots2) / (quantityLots1 + quantityLots2)
        );
        final double expectedYield = quantity1 * (price2 - price1);
        final Money price2Money = TestData.createMoney(currency, price2);
        final BigDecimal expectedQuantityLots = BigDecimal.valueOf(quantityLots1 + quantityLots2);
        final Position expectedPosition2 = Position.builder()
                .figi(figi)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(expectedQuantity)
                .averagePositionPrice(expectedAveragePositionPrice)
                .expectedYield(DecimalUtils.setDefaultScale(expectedYield))
                .currentPrice(price2Money)
                .quantityLots(expectedQuantityLots)
                .build();

        Mockito.when(fakeContext.getPosition(accountId, figi)).thenReturn(null, expectedPosition1);

        // action

        postOrder(accountId, figi, quantityLots1, OrderDirection.ORDER_DIRECTION_BUY, timestamp1, price1);
        postOrder(accountId, figi, quantityLots2, OrderDirection.ORDER_DIRECTION_BUY, timestamp2, price2);

        // assert

        verifyBalanceSet(accountId, currency, balance2);
        verifyBalanceSet(accountId, currency, balance3);
        verifyPositionAdded(accountId, figi, expectedPosition1);
        verifyPositionAdded(accountId, figi, expectedPosition2);
    }

    @Test
    void postOrder_buy_createsMultiplePositions_whenDifferentFigiessAreBought() {
        // arrange

        final String accountId = TestData.ACCOUNT_ID1;

        final Timestamp timestamp1 = TimestampUtils.newTimestamp(2020, 10, 5, 12);
        final Timestamp timestamp2 = TimestampUtils.plusMinutes(timestamp1, 5);
        final Timestamp timestamp3 = TimestampUtils.plusMinutes(timestamp1, 10);

        final String currency = TestShare2.CURRENCY;
        final double initialBalance = 1000000;
        final double balance1 = 989970;
        final double balance2 = 989669.1;
        final double balance3 = 939519.1;

        final String figi1 = TestShare2.FIGI;
        final String figi2 = TestShare3.FIGI;
        final String figi3 = TestShare4.FIGI;

        final double price1 = 1000;
        final double price2 = 100;
        final double price3 = 500;

        final int quantity1 = 10;
        final int quantity2 = 3;
        final int quantity3 = 100;

        final int quantityLots1 = 1;
        final int quantityLots2 = 3;
        final int quantityLots3 = 1;

        Mockito.when(fakeContext.getCurrentTimestamp()).thenReturn(timestamp1, timestamp1, timestamp2, timestamp2, timestamp3, timestamp3);
        mockBalances(accountId, currency, initialBalance, balance1, balance2);

        Mocker.mockShare(extInstrumentsService, TestShare2.SHARE);
        Mocker.mockShare(extInstrumentsService, TestShare3.SHARE);
        Mocker.mockShare(extInstrumentsService, TestShare4.SHARE);

        final Money price1Money = TestData.createMoney(currency, price1);
        final Position expectedPosition1 = Position.builder()
                .figi(figi1)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(BigDecimal.valueOf(quantity1))
                .averagePositionPrice(price1Money)
                .expectedYield(DecimalUtils.setDefaultScale(0))
                .currentPrice(price1Money)
                .quantityLots(BigDecimal.valueOf(quantityLots1))
                .build();

        final Money price2Money = TestData.createMoney(currency, price2);
        final Position expectedPosition2 = Position.builder()
                .figi(figi2)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(BigDecimal.valueOf(quantity2))
                .averagePositionPrice(price2Money)
                .expectedYield(DecimalUtils.setDefaultScale(0))
                .currentPrice(price2Money)
                .quantityLots(BigDecimal.valueOf(quantityLots2))
                .build();

        final Money price3Money = TestData.createMoney(currency, price3);
        final Position expectedPosition3 = Position.builder()
                .figi(figi3)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(BigDecimal.valueOf(quantity3))
                .averagePositionPrice(price3Money)
                .expectedYield(DecimalUtils.setDefaultScale(0))
                .currentPrice(price3Money)
                .quantityLots(BigDecimal.valueOf(quantityLots3))
                .build();

        Mockito.when(fakeContext.getPosition(accountId, figi1)).thenReturn(null, expectedPosition1);
        Mockito.when(fakeContext.getPosition(accountId, figi2)).thenReturn(null, expectedPosition2);
        Mockito.when(fakeContext.getPosition(accountId, figi3)).thenReturn(null, expectedPosition3);

        // action

        postOrder(accountId, figi1, quantityLots1, OrderDirection.ORDER_DIRECTION_BUY, timestamp1, price1);
        postOrder(accountId, figi2, quantityLots2, OrderDirection.ORDER_DIRECTION_BUY, timestamp2, price2);
        postOrder(accountId, figi3, quantityLots3, OrderDirection.ORDER_DIRECTION_BUY, timestamp3, price3);

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

        final String accountId = TestData.ACCOUNT_ID1;

        final Timestamp timestamp1 = TimestampUtils.newTimestamp(2020, 10, 5, 12);
        final Timestamp timestamp2 = TimestampUtils.plusMinutes(timestamp1, 5);
        final Timestamp timestamp3 = TimestampUtils.plusMinutes(timestamp1, 10);

        final String currency = TestShare2.CURRENCY;

        final double initialBalance = 1000000;
        final double balance1 = 979940;
        final double balance2 = 939820;

        final String figi = TestShare2.FIGI;
        final int lotSize = TestShare2.LOT;

        final double price1 = 1000;
        final double price2 = 4000;
        final double price3 = 3000;

        final int quantityLots1 = 2;
        final int quantityLots2 = 1;
        final int quantityLots3 = 4;

        final int quantity1 = quantityLots1 * lotSize;
        final int quantity2 = quantityLots2 * lotSize;

        Mockito.when(fakeContext.getCurrentTimestamp()).thenReturn(timestamp1, timestamp1, timestamp2, timestamp2, timestamp3, timestamp3);
        mockBalances(accountId, currency, initialBalance, balance1, balance2);
        Mocker.mockShare(extInstrumentsService, TestShare2.SHARE);

        final Money price1Money = TestData.createMoney(currency, price1);
        final Position expectedPosition1 = Position.builder()
                .figi(figi)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(BigDecimal.valueOf(quantity1))
                .averagePositionPrice(price1Money)
                .expectedYield(DecimalUtils.setDefaultScale(0))
                .currentPrice(price1Money)
                .quantityLots(BigDecimal.valueOf(quantityLots1))
                .build();

        final BigDecimal expectedQuantity = BigDecimal.valueOf(quantity1 + quantity2);
        final Money expectedAveragePositionPrice = TestData.createMoney(
                currency,
                (price1 * quantityLots1 + price2 * quantityLots2) / (quantityLots1 + quantityLots2)
        );
        final BigDecimal expectedYield = DecimalUtils.setDefaultScale(quantity1 * (price2 - price1));
        final Money price2Money = TestData.createMoney(currency, price2);
        final BigDecimal expectedQuantityLots = BigDecimal.valueOf(quantityLots1 + quantityLots2);
        final Position expectedPosition2 = Position.builder()
                .figi(figi)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(expectedQuantity)
                .averagePositionPrice(expectedAveragePositionPrice)
                .expectedYield(expectedYield)
                .currentPrice(price2Money)
                .quantityLots(expectedQuantityLots)
                .build();

        Mockito.when(fakeContext.getPosition(accountId, figi)).thenReturn(null, expectedPosition1, expectedPosition2);

        // action & assert

        postOrder(accountId, figi, quantityLots1, OrderDirection.ORDER_DIRECTION_BUY, timestamp1, price1);
        postOrder(accountId, figi, quantityLots2, OrderDirection.ORDER_DIRECTION_BUY, timestamp2, price2);
        final Executable sellExecutable = () -> postOrder(accountId, figi, quantityLots3, OrderDirection.ORDER_DIRECTION_SELL, timestamp3, price3);
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

        final String accountId = TestData.ACCOUNT_ID1;

        final Timestamp timestamp1 = TimestampUtils.newTimestamp(2020, 10, 5, 12);
        final Timestamp timestamp2 = TimestampUtils.plusMinutes(timestamp1, 5);
        final Timestamp timestamp3 = TimestampUtils.plusMinutes(timestamp1, 10);

        final String currency = TestShare2.CURRENCY;

        final double initialBalance = 1000000;
        final double balance1 = 979940;
        final double balance2 = 939820;
        final double balance3 = 1029550;

        final String figi = TestShare2.FIGI;
        final int lotSize = TestShare2.LOT;

        final double price1 = 1000;
        final double price2 = 4000;
        final double price3 = 3000;

        final int quantityLots1 = 2;
        final int quantityLots2 = 1;
        final int quantityLots3 = 3;

        final int quantity1 = quantityLots1 * lotSize;
        final int quantity2 = quantityLots2 * lotSize;

        Mockito.when(fakeContext.getCurrentTimestamp()).thenReturn(timestamp1, timestamp1, timestamp2, timestamp2, timestamp3, timestamp3);
        mockBalances(accountId, currency, initialBalance, balance1, balance2);
        Mocker.mockShare(extInstrumentsService, TestShare2.SHARE);

        final Money price1Money = TestData.createMoney(currency, price1);
        final Position expectedPosition1 = Position.builder()
                .figi(figi)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(BigDecimal.valueOf(quantity1))
                .averagePositionPrice(price1Money)
                .expectedYield(DecimalUtils.setDefaultScale(0))
                .currentPrice(price1Money)
                .quantityLots(BigDecimal.valueOf(quantityLots1))
                .build();

        final BigDecimal expectedQuantity = BigDecimal.valueOf(quantity1 + quantity2);
        final Money expectedAveragePositionPrice = TestData.createMoney(
                currency,
                (price1 * quantityLots1 + price2 * quantityLots2) / (quantityLots1 + quantityLots2)
        );
        final BigDecimal expectedYield = DecimalUtils.setDefaultScale(quantity1 * (price2 - price1));
        final Money price2Money = TestData.createMoney(currency, price2);
        final BigDecimal expectedQuantityLots = BigDecimal.valueOf(quantityLots1 + quantityLots2);
        final Position expectedPosition2 = Position.builder()
                .figi(figi)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(expectedQuantity)
                .averagePositionPrice(expectedAveragePositionPrice)
                .expectedYield(expectedYield)
                .currentPrice(price2Money)
                .quantityLots(expectedQuantityLots)
                .build();

        Mockito.when(fakeContext.getPosition(accountId, figi)).thenReturn(null, expectedPosition1, expectedPosition2);

        // action

        postOrder(accountId, figi, quantityLots1, OrderDirection.ORDER_DIRECTION_BUY, timestamp1, price1);
        postOrder(accountId, figi, quantityLots2, OrderDirection.ORDER_DIRECTION_BUY, timestamp2, price2);
        postOrder(accountId, figi, quantityLots3, OrderDirection.ORDER_DIRECTION_SELL, timestamp3, price3);

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

        final String accountId = TestData.ACCOUNT_ID1;

        final Timestamp timestamp1 = TimestampUtils.newTimestamp(2020, 10, 5, 12);
        final Timestamp timestamp2 = TimestampUtils.plusMinutes(timestamp1, 5);
        final Timestamp timestamp3 = TimestampUtils.plusMinutes(timestamp1, 10);

        final String currency = TestShare2.CURRENCY;

        final double initialBalance = 1000000;
        final double balance1 = 979940;
        final double balance2 = 939820;
        final double balance3 = 969730;

        final String figi = TestShare2.FIGI;
        final int lotSize = TestShare2.LOT;

        final double price1 = 1000;
        final double price2 = 4000;
        final double price3 = 3000;

        final int quantityLots1 = 2;
        final int quantityLots2 = 1;
        final int quantityLots3 = 1;

        final int quantity1 = quantityLots1 * lotSize;
        final int quantity2 = quantityLots2 * lotSize;
        final int quantity3 = quantityLots3 * lotSize;

        Mockito.when(fakeContext.getCurrentTimestamp()).thenReturn(timestamp1, timestamp1, timestamp2, timestamp2, timestamp3, timestamp3);
        mockBalances(accountId, currency, initialBalance, balance1, balance2);
        Mocker.mockShare(extInstrumentsService, TestShare2.SHARE);

        final Money price1Money = TestData.createMoney(currency, price1);
        final Position expectedPosition1 = Position.builder()
                .figi(figi)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(BigDecimal.valueOf(quantity1))
                .averagePositionPrice(price1Money)
                .expectedYield(BigDecimal.ZERO)
                .currentPrice(price1Money)
                .quantityLots(BigDecimal.valueOf(quantityLots1))
                .build();

        final BigDecimal expectedQuantity2 = BigDecimal.valueOf(quantity1 + quantity2);
        final double expectedAveragePositionPrice2Value = (price1 * quantityLots1 + price2 * quantityLots2) / (quantityLots1 + quantityLots2);
        final Money expectedAveragePositionPrice2 = TestData.createMoney(currency, expectedAveragePositionPrice2Value);
        final BigDecimal expectedYield2 = DecimalUtils.setDefaultScale(quantity1 * (price2 - price1));
        final Money price2Money = TestData.createMoney(currency, price2);
        final BigDecimal expectedQuantityLots2 = BigDecimal.valueOf(quantityLots1 + quantityLots2);
        final Position expectedPosition2 = Position.builder()
                .figi(figi)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(expectedQuantity2)
                .averagePositionPrice(expectedAveragePositionPrice2)
                .expectedYield(expectedYield2)
                .currentPrice(price2Money)
                .quantityLots(expectedQuantityLots2)
                .build();

        final BigDecimal expectedQuantity3 = BigDecimal.valueOf(quantity1 + quantity2 - quantity3);
        final BigDecimal expectedYield3 = DecimalUtils.setDefaultScale(
                (price3 - expectedAveragePositionPrice2Value) * (quantityLots1 + quantityLots2 - quantityLots3) * lotSize
        );
        final Money price3Money = TestData.createMoney(currency, price3);
        final BigDecimal expectedQuantityLots3 = BigDecimal.valueOf(quantityLots1 + quantityLots2 - quantityLots3);
        final Position expectedPosition3 = Position.builder()
                .figi(figi)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(expectedQuantity3)
                .averagePositionPrice(expectedAveragePositionPrice2)
                .expectedYield(expectedYield3)
                .currentPrice(price3Money)
                .quantityLots(expectedQuantityLots3)
                .build();

        Mockito.when(fakeContext.getPosition(accountId, figi)).thenReturn(null, expectedPosition1, expectedPosition2, expectedPosition3);

        // action

        postOrder(accountId, figi, quantityLots1, OrderDirection.ORDER_DIRECTION_BUY, timestamp1, price1);
        postOrder(accountId, figi, quantityLots2, OrderDirection.ORDER_DIRECTION_BUY, timestamp2, price2);
        postOrder(accountId, figi, quantityLots3, OrderDirection.ORDER_DIRECTION_SELL, timestamp3, price3);

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
        final BigDecimal[] bigDecimalBalances = Arrays.stream(balances)
                .mapToObj(BigDecimal::valueOf)
                .toArray(BigDecimal[]::new);
        Mockito.when(fakeContext.getBalance(accountId, currency)).thenReturn(DecimalUtils.setDefaultScale(balance1), bigDecimalBalances);
    }

    @SuppressWarnings("SameParameterValue")
    private void postOrder(
            final String accountId,
            final String figi,
            final int quantityLots,
            final OrderDirection direction,
            final Timestamp timestamp,
            final double price
    ) {
        Mockito.when(extMarketDataService.getLastPrice(figi, timestamp))
                .thenReturn(DecimalUtils.setDefaultScale(price));

        fakeExtOrdersService.postOrder(accountId, figi, quantityLots, null, direction, OrderType.ORDER_TYPE_MARKET, null);
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
        final String accountId = TestData.ACCOUNT_ID1;
        final String orderId = "orderId";

        AssertUtils.assertThrowsWithMessage(
                UnsupportedOperationException.class,
                () -> fakeExtOrdersService.cancelOrder(accountId, orderId),
                "Back test does not support cancelling of orders"
        );
    }

}