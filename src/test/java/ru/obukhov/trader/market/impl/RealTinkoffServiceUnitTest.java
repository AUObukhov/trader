package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus;
import ru.tinkoff.piapi.contract.v1.OrderStage;
import ru.tinkoff.piapi.contract.v1.OrderState;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.OrdersService;
import ru.tinkoff.piapi.core.UsersService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class RealTinkoffServiceUnitTest {

    @Mock
    private InstrumentsService instrumentsService;
    @Mock
    private OperationsService operationsService;
    @Mock
    private OrdersService ordersService;
    @Mock
    private UsersService usersService;
    @Mock
    private ExtInstrumentsService extInstrumentsService;

    @InjectMocks
    private RealTinkoffService realTinkoffService;

    // region OrdersContext methods tests

    @Test
    void getOrders() {

        // arrange

        final String accountId = "2000124699";

        // todo realistic data (copy from OrderMapperTest)
        final Currency currency1 = Currency.EUR;
        final String orderId1 = "orderId1";
        final OrderExecutionReportStatus executionReportStatus1 = OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_FILL;
        final int lotsRequested1 = 1;
        final int lotsExecuted1 = 2;
        final double initialOrderPrice1 = 3;
        final double totalOrderAmount1 = 4;
        final double averagePositionPrice1 = 5;
        final double initialCommission1 = 6;
        final double executedCommission1 = 7;
        final String figi1 = "figi1";
        final OrderDirection orderDirection1 = OrderDirection.ORDER_DIRECTION_BUY;
        final double initialSecurityPrice1 = 8;
        final List<OrderStage> stages1 = List.of(
                TestData.createOrderStage(currency1, 9, 10, "tradeId1"),
                TestData.createOrderStage(currency1, 11, 12, "tradeId1")
        );
        final double serviceCommission1 = 13;
        final ru.tinkoff.piapi.contract.v1.OrderType orderType1 = OrderType.ORDER_TYPE_MARKET;
        final OffsetDateTime orderDate1 = OffsetDateTime.now();

        final OrderState orderState1 = TestData.createOrderState(
                currency1,
                orderId1,
                executionReportStatus1,
                lotsRequested1,
                lotsExecuted1,
                initialOrderPrice1,
                totalOrderAmount1,
                averagePositionPrice1,
                initialCommission1,
                executedCommission1,
                figi1,
                orderDirection1,
                initialSecurityPrice1,
                stages1,
                serviceCommission1,
                orderType1,
                orderDate1
        );

        final Currency currency2 = Currency.USD;
        final String orderId2 = "orderId2";
        final OrderExecutionReportStatus executionReportStatus2 = OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_NEW;
        final int lotsRequested2 = 14;
        final int lotsExecuted2 = 15;
        final double initialOrderPrice2 = 16;
        final double totalOrderAmount2 = 17;
        final double averagePositionPrice2 = 18;
        final double initialCommission2 = 19;
        final double executedCommission2 = 20;
        final String figi2 = "figi2";
        final OrderDirection orderDirection2 = OrderDirection.ORDER_DIRECTION_SELL;
        final double initialSecurityPrice2 = 21;
        final List<OrderStage> stages2 = List.of(
                TestData.createOrderStage(currency2, 22, 23, "tradeId2"),
                TestData.createOrderStage(currency2, 24, 25, "tradeId2")
        );
        final double serviceCommission2 = 26;
        final ru.tinkoff.piapi.contract.v1.OrderType orderType2 = OrderType.ORDER_TYPE_LIMIT;
        final OffsetDateTime orderDate2 = OffsetDateTime.now();

        final OrderState orderState2 = TestData.createOrderState(
                currency2,
                orderId2,
                executionReportStatus2,
                lotsRequested2,
                lotsExecuted2,
                initialOrderPrice2,
                totalOrderAmount2,
                averagePositionPrice2,
                initialCommission2,
                executedCommission2,
                figi2,
                orderDirection2,
                initialSecurityPrice2,
                stages2,
                serviceCommission2,
                orderType2,
                orderDate2
        );

        final List<OrderState> orderStates = List.of(orderState1, orderState2);
        Mockito.when(ordersService.getOrdersSync(accountId)).thenReturn(orderStates);

        // action

        final List<Order> result = realTinkoffService.getOrders(accountId);

        // assert

        final Order order1 = TestData.createOrder(
                currency1,
                orderId1,
                executionReportStatus1,
                lotsExecuted1,
                initialOrderPrice1,
                totalOrderAmount1,
                averagePositionPrice1,
                executedCommission1,
                figi1,
                orderDirection1,
                initialSecurityPrice1,
                serviceCommission1,
                orderType1,
                orderDate1
        );
        final Order order2 = TestData.createOrder(
                currency2,
                orderId2,
                executionReportStatus2,
                lotsExecuted2,
                initialOrderPrice2,
                totalOrderAmount2,
                averagePositionPrice2,
                executedCommission2,
                figi2,
                orderDirection2,
                initialSecurityPrice2,
                serviceCommission2,
                orderType2,
                orderDate2
        );
        final List<Order> expectedResult = List.of(order1, order2);

        AssertUtils.assertEquals(expectedResult, result);
    }

    @Test
    void postOrder() {
        final String accountId = "2000124699";
        final String ticker = "ticker";
        final String figi = "figi";
        final long quantityLots = 10;
        final BigDecimal price = BigDecimal.valueOf(200);
        final OrderDirection direction = OrderDirection.ORDER_DIRECTION_BUY;
        final OrderType type = OrderType.ORDER_TYPE_MARKET;
        final String orderId = "orderId";

        Mockito.when(extInstrumentsService.getFigiByTicker(ticker)).thenReturn(figi);

        PostOrderResponse response = TestData.createPostOrderResponse(
                Currency.USD,
                2000,
                10,
                20,
                orderId,
                quantityLots,
                figi,
                direction,
                type
        );
        Mockito.when(ordersService.postOrderSync(figi, quantityLots, DecimalUtils.toQuotation(price), direction, accountId, type, orderId))
                .thenReturn(response);

        final PostOrderResponse result = realTinkoffService.postOrder(accountId, ticker, quantityLots, price, direction, type, orderId);

        Assertions.assertSame(response, result);
    }

    @Test
    void cancelOrder() {
        final String accountId = "2000124699";
        final String orderId = "orderId";

        realTinkoffService.cancelOrder(accountId, orderId);

        Mockito.verify(ordersService, Mockito.times(1)).cancelOrderSync(accountId, orderId);
    }

    // endregion

    @Test
    void getCurrentDateTime_returnsCurrentDateTime() {
        final OffsetDateTime now = OffsetDateTime.now();

        final OffsetDateTime currentDateTime = realTinkoffService.getCurrentDateTime();

        final long delay = Duration.between(now, currentDateTime).toMillis();

        Assertions.assertTrue(delay >= 0);

        final int maxDelay = 5;
        Assertions.assertTrue(delay < maxDelay);
    }

}