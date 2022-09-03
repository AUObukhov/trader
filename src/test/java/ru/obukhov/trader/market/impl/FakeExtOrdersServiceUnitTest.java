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
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.matchers.BigDecimalMatcher;
import ru.obukhov.trader.test.utils.matchers.PortfolioPositionMatcher;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;

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
        fakeExtOrdersService = new FakeExtOrdersService(fakeContext, extInstrumentsService, extMarketDataService, COMMISSION);
    }

    // region getOrders tests

    @Test
    void getOrders_byAccountId_returnsEmptyList() {
        final String accountId = "2000124699";

        List<Order> orders = fakeExtOrdersService.getOrders(accountId);

        Assertions.assertTrue(orders.isEmpty());
    }

    @Test
    void getOrders_byAccountIdAndTicker_returnsEmptyList() {
        final String accountId = "2000124699";
        final String figi = "figi";
        final String ticker = "ticker";

        Mockito.when(extInstrumentsService.getFigiByTicker(ticker)).thenReturn(figi);

        List<Order> orders = fakeExtOrdersService.getOrders(accountId, ticker);

        Assertions.assertTrue(orders.isEmpty());
    }

    // endregion

    // region postOrder tests. Implicit tests for getPortfolioPositions

    @Test
    void postOrder_buy_throwsIllegalArgumentException_whenNotEnoughBalance() {
        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final String ticker = "ticker";

        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(dateTime);
        mockBalances(accountId, currency, 1000);
        Mocker.mockShare(extInstrumentsService, ticker, Currency.RUB, 10);

        final Executable executable = () -> postOrder(accountId, ticker, 2, OrderDirection.ORDER_DIRECTION_BUY, dateTime, 500);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "balance can't be negative");

        Mockito.verify(fakeContext, Mockito.never())
                .setBalance(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void postOrder_buy_createsNewPosition_whenNoPositions() {
        // arrange

        final String accountId = "2000124699";
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final Currency currency = Currency.RUB;
        final String ticker = "ticker";
        final int lotSize = 10;
        final int quantityLots = 1;
        final double currentPrice = 1000;
        final double balance1 = 1000000;
        final double balance2 = balance1 - currentPrice * lotSize * quantityLots * COMMISSION_COEF;

        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(dateTime);
        mockBalances(accountId, currency, balance1);
        Mockito.when(fakeContext.getPosition(accountId, ticker)).thenReturn(null);
        Mocker.mockShare(extInstrumentsService, ticker, currency, lotSize);

        // action

        postOrder(accountId, ticker, quantityLots, OrderDirection.ORDER_DIRECTION_BUY, dateTime, currentPrice);

        // assert

        verifyBalanceSet(accountId, currency, balance2);
        final PortfolioPosition expectedPosition = TestData.createPortfolioPosition(
                ticker,
                InstrumentType.STOCK,
                quantityLots,
                lotSize,
                currency,
                currentPrice,
                0,
                currentPrice
        );
        verifyPositionAdded(accountId, ticker, expectedPosition);
    }

    @Test
    void postOrder_buy_addsValueToExistingPosition_whenPositionAlreadyExists() {
        // arrange

        final String accountId = "2000124699";
        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(5);
        final Currency currency = Currency.RUB;
        final String ticker = "ticker";
        final int lotSize = 10;
        final int quantityLots1 = 2;
        final int quantityLots2 = 1;
        final double price1 = 1000;
        final double price2 = 4000;
        final double balance1 = 1000000;
        final double balance2 = balance1 - price1 * lotSize * quantityLots1 * COMMISSION_COEF;
        final double balance3 = balance2 - price2 * lotSize * quantityLots2 * COMMISSION_COEF;

        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(dateTime1, dateTime1, dateTime2, dateTime2);
        mockBalances(accountId, currency, balance1, balance2);
        Mocker.mockShare(extInstrumentsService, ticker, currency, lotSize);

        final PortfolioPosition expectedPosition1 = TestData.createPortfolioPosition(
                ticker,
                InstrumentType.STOCK,
                quantityLots1,
                lotSize,
                currency,
                price1,
                0,
                price1
        );

        final double expectedAveragePositionPrice = (price1 * quantityLots1 + price2 * quantityLots2) / (quantityLots1 + quantityLots2);
        final double expectedExpectedYield = quantityLots1 * (price2 - price1) * lotSize;
        final PortfolioPosition expectedPosition2 = TestData.createPortfolioPosition(
                ticker,
                InstrumentType.STOCK,
                quantityLots1 + quantityLots2,
                lotSize,
                currency,
                expectedAveragePositionPrice,
                expectedExpectedYield,
                price2
        );

        Mockito.when(fakeContext.getPosition(accountId, ticker)).thenReturn(null, expectedPosition1);

        // action

        postOrder(accountId, ticker, quantityLots1, OrderDirection.ORDER_DIRECTION_BUY, dateTime1, price1);
        postOrder(accountId, ticker, quantityLots2, OrderDirection.ORDER_DIRECTION_BUY, dateTime2, price2);

        // assert

        verifyBalanceSet(accountId, currency, balance2);
        verifyBalanceSet(accountId, currency, balance3);
        verifyPositionAdded(accountId, ticker, expectedPosition1);
        verifyPositionAdded(accountId, ticker, expectedPosition2);
    }

    @Test
    void postOrder_buy_createsMultiplePositions_whenDifferentTickersAreBought() {
        // arrange

        final String accountId = "2000124699";

        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(5);
        final OffsetDateTime dateTime3 = dateTime1.plusMinutes(10);

        final Currency currency = Currency.RUB;
        final double initialBalance = 1000000;
        final double balance1 = 989970;
        final double balance2 = 989368.2;
        final double balance3 = 988866.7;

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final String ticker3 = "ticker3";

        final int lotSize1 = 10;
        final int lotSize2 = 2;
        final int lotSize3 = 1;

        final double price1 = 1000;
        final double price2 = 100;
        final double price3 = 500;

        final int quantityLots1 = 1;
        final int quantityLots2 = 3;
        final int quantityLots3 = 1;

        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(dateTime1, dateTime1, dateTime2, dateTime2, dateTime3, dateTime3);
        mockBalances(accountId, currency, initialBalance, balance1, balance2);

        Mocker.mockShare(extInstrumentsService, ticker1, currency, lotSize1);
        Mocker.mockShare(extInstrumentsService, ticker2, currency, lotSize2);
        Mocker.mockShare(extInstrumentsService, ticker3, currency, lotSize3);

        final PortfolioPosition expectedPosition1 = TestData.createPortfolioPosition(
                ticker1,
                InstrumentType.STOCK,
                quantityLots1,
                lotSize1,
                currency,
                price1,
                0,
                price1
        );
        final PortfolioPosition expectedPosition2 = TestData.createPortfolioPosition(
                ticker2,
                InstrumentType.STOCK,
                quantityLots2,
                lotSize2,
                currency,
                price2,
                0,
                price2
        );
        final PortfolioPosition expectedPosition3 = TestData.createPortfolioPosition(
                ticker3,
                InstrumentType.STOCK,
                quantityLots3,
                lotSize3,
                currency,
                price3,
                0,
                price3
        );

        Mockito.when(fakeContext.getPosition(accountId, ticker1)).thenReturn(null, expectedPosition1);
        Mockito.when(fakeContext.getPosition(accountId, ticker2)).thenReturn(null, expectedPosition2);
        Mockito.when(fakeContext.getPosition(accountId, ticker3)).thenReturn(null, expectedPosition3);

        // action

        postOrder(accountId, ticker1, quantityLots1, OrderDirection.ORDER_DIRECTION_BUY, dateTime1, price1);
        postOrder(accountId, ticker2, quantityLots2, OrderDirection.ORDER_DIRECTION_BUY, dateTime2, price2);
        postOrder(accountId, ticker3, quantityLots3, OrderDirection.ORDER_DIRECTION_BUY, dateTime3, price3);

        // assert

        verifyPositionAdded(accountId, ticker1, expectedPosition1);
        verifyPositionAdded(accountId, ticker2, expectedPosition2);
        verifyPositionAdded(accountId, ticker3, expectedPosition3);

        verifyBalanceSet(accountId, currency, balance1);
        verifyBalanceSet(accountId, currency, balance2);
        verifyBalanceSet(accountId, currency, balance3);
    }

    @Test
    void postOrder_sell_throwsIllegalArgumentException_whenSellsMoreLotsThanExists() {
        // arrange

        final String accountId = "2000124699";

        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(5);
        final OffsetDateTime dateTime3 = dateTime1.plusMinutes(10);

        final Currency currency = Currency.RUB;

        final double initialBalance = 1000000;
        final double balance1 = 979940;
        final double balance2 = 939820;

        final String ticker = "ticker";
        final int lotSize = 10;

        final double price1 = 1000;
        final double price2 = 4000;
        final double price3 = 3000;

        final int quantityLots1 = 2;
        final int quantityLots2 = 1;
        final int quantityLots3 = 4;

        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(dateTime1, dateTime1, dateTime2, dateTime2, dateTime3, dateTime3);
        mockBalances(accountId, currency, initialBalance, balance1, balance2);
        Mocker.mockShare(extInstrumentsService, ticker, Currency.RUB, lotSize);

        final PortfolioPosition expectedPosition1 = TestData.createPortfolioPosition(
                ticker,
                InstrumentType.STOCK,
                quantityLots1,
                lotSize,
                currency,
                price1,
                0,
                price1
        );
        final double expectedAveragePositionPrice = (price1 * quantityLots1 + price2 * quantityLots2) / (quantityLots1 + quantityLots2);
        final double expectedExpectedYield = quantityLots1 * (price2 - price1) * lotSize;
        final PortfolioPosition expectedPosition2 = TestData.createPortfolioPosition(
                ticker,
                InstrumentType.STOCK,
                quantityLots1 + quantityLots2,
                lotSize,
                currency,
                expectedAveragePositionPrice,
                expectedExpectedYield,
                price2
        );

        Mockito.when(fakeContext.getPosition(accountId, ticker)).thenReturn(null, expectedPosition1, expectedPosition2);

        // action & assert

        postOrder(accountId, ticker, quantityLots1, OrderDirection.ORDER_DIRECTION_BUY, dateTime1, price1);
        postOrder(accountId, ticker, quantityLots2, OrderDirection.ORDER_DIRECTION_BUY, dateTime2, price2);
        final Executable sellExecutable = () -> postOrder(accountId, ticker, quantityLots3, OrderDirection.ORDER_DIRECTION_SELL, dateTime3, price3);
        final String expectedMessage = "lotsCount 4 can't be greater than existing position lots count 3";
        Assertions.assertThrows(IllegalArgumentException.class, sellExecutable, expectedMessage);

        verifyPositionAdded(accountId, ticker, expectedPosition1);
        verifyPositionAdded(accountId, ticker, expectedPosition2);

        verifyBalanceSet(accountId, currency, balance1);
        verifyBalanceSet(accountId, currency, balance2);
    }

    @Test
    void postOrder_sell_removesPosition_whenAllLotsAreSold() {
        // arrange

        final String accountId = "2000124699";

        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(5);
        final OffsetDateTime dateTime3 = dateTime1.plusMinutes(10);

        final Currency currency = Currency.RUB;

        final double initialBalance = 1000000;
        final double balance1 = 979940;
        final double balance2 = 939820;
        final double balance3 = 1029550;

        final String ticker = "ticker";
        final int lotSize = 10;

        final double price1 = 1000;
        final double price2 = 4000;
        final double price3 = 3000;

        final int quantityLots1 = 2;
        final int quantityLots2 = 1;
        final int quantityLots3 = 3;

        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(dateTime1, dateTime1, dateTime2, dateTime2, dateTime3, dateTime3);
        mockBalances(accountId, currency, initialBalance, balance1, balance2);
        Mocker.mockShare(extInstrumentsService, ticker, Currency.RUB, lotSize);

        final PortfolioPosition expectedPosition1 = TestData.createPortfolioPosition(
                ticker,
                InstrumentType.STOCK,
                quantityLots1,
                lotSize,
                currency,
                price1,
                0,
                price1
        );
        final double expectedAveragePositionPrice = (price1 * quantityLots1 + price2 * quantityLots2) / (quantityLots1 + quantityLots2);
        final double expectedExpectedYield = quantityLots1 * (price2 - price1) * lotSize;
        final PortfolioPosition expectedPosition2 = TestData.createPortfolioPosition(
                ticker,
                InstrumentType.STOCK,
                quantityLots1 + quantityLots2,
                lotSize,
                currency,
                expectedAveragePositionPrice,
                expectedExpectedYield,
                price2
        );

        Mockito.when(fakeContext.getPosition(accountId, ticker)).thenReturn(null, expectedPosition1, expectedPosition2);

        // action

        postOrder(accountId, ticker, quantityLots1, OrderDirection.ORDER_DIRECTION_BUY, dateTime1, price1);
        postOrder(accountId, ticker, quantityLots2, OrderDirection.ORDER_DIRECTION_BUY, dateTime2, price2);
        postOrder(accountId, ticker, quantityLots3, OrderDirection.ORDER_DIRECTION_SELL, dateTime3, price3);

        // assert

        verifyPositionAdded(accountId, ticker, expectedPosition1);
        verifyPositionAdded(accountId, ticker, expectedPosition2);
        Mockito.verify(fakeContext, Mockito.times(1)).removePosition(accountId, ticker);

        verifyBalanceSet(accountId, currency, balance1);
        verifyBalanceSet(accountId, currency, balance2);
        verifyBalanceSet(accountId, currency, balance3);
    }

    @Test
    void postOrder_sell_reducesLotsCount() {
        // arrange

        final String accountId = "2000124699";

        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetDateTime dateTime2 = dateTime1.plusMinutes(5);
        final OffsetDateTime dateTime3 = dateTime1.plusMinutes(10);

        final Currency currency = Currency.RUB;

        final double initialBalance = 1000000;
        final double balance1 = 979940;
        final double balance2 = 939820;
        final double balance3 = 969730;

        final int lotSize = 10;
        final String ticker = "ticker";

        final double price1 = 1000;
        final double price2 = 4000;
        final double price3 = 3000;

        final int quantityLots1 = 2;
        final int quantityLots2 = 1;
        final int quantityLots3 = 1;

        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(dateTime1, dateTime1, dateTime2, dateTime2, dateTime3, dateTime3);
        mockBalances(accountId, currency, initialBalance, balance1, balance2);
        Mocker.mockShare(extInstrumentsService, ticker, Currency.RUB, lotSize);

        final PortfolioPosition expectedPosition1 = TestData.createPortfolioPosition(
                ticker,
                InstrumentType.STOCK,
                quantityLots1,
                lotSize,
                currency,
                price1,
                0,
                price1
        );
        final double expectedAveragePositionPrice2 = (price1 * quantityLots1 + price2 * quantityLots2) / (quantityLots1 + quantityLots2);
        final double expectedExpectedYield2 = quantityLots1 * (price2 - price1) * lotSize;
        final PortfolioPosition expectedPosition2 = TestData.createPortfolioPosition(
                ticker,
                InstrumentType.STOCK,
                quantityLots1 + quantityLots2,
                lotSize,
                currency,
                expectedAveragePositionPrice2,
                expectedExpectedYield2,
                price2
        );

        final double expectedExpectedYield3 = (price3 - expectedAveragePositionPrice2) * (quantityLots1 + quantityLots2 - quantityLots3) * lotSize;
        final PortfolioPosition expectedPosition3 = TestData.createPortfolioPosition(
                ticker,
                InstrumentType.STOCK,
                quantityLots1 + quantityLots2 - quantityLots3,
                lotSize,
                currency,
                expectedAveragePositionPrice2,
                expectedExpectedYield3,
                price3
        );

        Mockito.when(fakeContext.getPosition(accountId, ticker)).thenReturn(null, expectedPosition1, expectedPosition2, expectedPosition3);

        // action

        postOrder(accountId, ticker, quantityLots1, OrderDirection.ORDER_DIRECTION_BUY, dateTime1, price1);
        postOrder(accountId, ticker, quantityLots2, OrderDirection.ORDER_DIRECTION_BUY, dateTime2, price2);
        postOrder(accountId, ticker, quantityLots3, OrderDirection.ORDER_DIRECTION_SELL, dateTime3, price3);

        // assert

        verifyPositionAdded(accountId, ticker, expectedPosition1);
        verifyPositionAdded(accountId, ticker, expectedPosition2);
        verifyPositionAdded(accountId, ticker, expectedPosition3);

        verifyBalanceSet(accountId, currency, balance1);
        verifyBalanceSet(accountId, currency, balance2);
        verifyBalanceSet(accountId, currency, balance3);
    }

    private void mockBalances(final String accountId, final Currency currency, double balance1, double... balances) {
        final BigDecimal[] bigDecimalBalances = Arrays.stream(balances)
                .mapToObj(BigDecimal::valueOf)
                .toArray(BigDecimal[]::new);
        Mockito.when(fakeContext.getBalance(accountId, currency)).thenReturn(BigDecimal.valueOf(balance1), bigDecimalBalances);
    }

    private void postOrder(
            final String accountId,
            final String ticker,
            final int quantityLots,
            final OrderDirection direction,
            final OffsetDateTime dateTime,
            final double price
    ) {
        Mockito.when(extMarketDataService.getLastPrice(ticker, dateTime))
                .thenReturn(DecimalUtils.setDefaultScale(price));

        fakeExtOrdersService.postOrder(accountId, ticker, quantityLots, null, direction, OrderType.ORDER_TYPE_MARKET, null);
    }

    private void verifyBalanceSet(final String accountId, final Currency currency, final double balance) {
        Mockito.verify(fakeContext, Mockito.times(1))
                .setBalance(Mockito.eq(accountId), Mockito.eq(currency), ArgumentMatchers.argThat(BigDecimalMatcher.of(balance)));
    }

    private void verifyPositionAdded(final String accountId, final String ticker, final PortfolioPosition position) {
        Mockito.verify(fakeContext, Mockito.times(1))
                .addPosition(Mockito.eq(accountId), Mockito.eq(ticker), ArgumentMatchers.argThat(PortfolioPositionMatcher.of(position)));
    }

    // endregion

    @Test
    void cancelOrder_throwsUnsupportedOperationException() {
        final String accountId = "2000124699";
        final String orderId = "orderId";

        Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> fakeExtOrdersService.cancelOrder(accountId, orderId),
                "Back test does not support cancelling of orders"
        );
    }

}