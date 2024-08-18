package ru.obukhov.trader.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import ru.obukhov.trader.TokenValidationStartupListener;
import ru.obukhov.trader.common.model.ExecutionResult;
import ru.obukhov.trader.common.util.ExecutionUtils;
import ru.obukhov.trader.config.properties.ApiProperties;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.instrument.TestInstruments;
import ru.obukhov.trader.test.utils.model.order_state.TestOrderStates;
import ru.tinkoff.piapi.contract.v1.Account;
import ru.tinkoff.piapi.contract.v1.BrokerReportResponse;
import ru.tinkoff.piapi.contract.v1.GetTradingStatusResponse;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderState;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PositionsResponse;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.OrdersService;
import ru.tinkoff.piapi.core.UsersService;
import ru.tinkoff.piapi.core.models.Positions;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
public class ApiCallsThrottlingIntegrationTest {

    @Autowired
    private ApiProperties apiProperties;

    @SpyBean
    public static InstrumentsService instrumentsService;
    @SpyBean
    public static UsersService usersService;
    @SpyBean
    public static OperationsService operationsService;
    @SpyBean
    public static MarketDataService marketDataService;
    @SpyBean
    public static OrdersService ordersService;

    @MockBean
    @SuppressWarnings("unused")
    public static TokenValidationStartupListener tokenValidationStartupListener;

    @Test
    void instrumentsService() throws InterruptedException {
        final Instrument instrument = TestInstruments.APPLE.tInstrument();
        final String figi = instrument.getFigi();

        Mockito.doReturn(instrument).when(instrumentsService).getInstrumentByFigiSync(figi);

        waitForThrottlingCounters();

        testThrottling(() -> instrumentsService.getInstrumentByFigiSync(figi), 200, instrument);
    }

    @Test
    void usersService() throws InterruptedException {
        final List<Account> accounts = List.of(TestAccounts.IIS.tAccount(), TestAccounts.TINKOFF.tAccount());
        Mockito.doReturn(accounts).when(usersService).getAccountsSync();

        waitForThrottlingCounters();

        testThrottling(() -> usersService.getAccountsSync(), 100, accounts);
    }

    // region operationsService tests

    @Test
    void operationsService() throws InterruptedException {
        final String accountId = TestAccounts.IIS.getId();
        final Instant from = Instant.now();
        final Instant to = Instant.now();

        Positions positions = Positions.fromResponse(PositionsResponse.getDefaultInstance());
        Mockito.doReturn(positions).when(operationsService).getPositionsSync(accountId);

        Mockito.doReturn(BrokerReportResponse.getDefaultInstance()).when(operationsService).getBrokerReportSync(accountId, from, to);

        waitForThrottlingCounters();

        final Runnable runnable = () -> operationsService.getBrokerReportSync(accountId, from, to);
        ExecutionUtils.run(runnable, ApiCallsThrottling.OPERATIONS_SERVICE_BROKER_REPORT_LIMIT);

        testThrottling(() -> operationsService.getPositionsSync(accountId), ApiCallsThrottling.OPERATIONS_SERVICE_LIMIT, positions);
    }

    @Test
    void operationsService_getBrokerReportSync() throws InterruptedException {
        final String accountId = TestAccounts.IIS.getId();
        final Instant from = Instant.now();
        final Instant to = Instant.now();

        Mockito.doReturn(null).when(operationsService).getPositionsSync(accountId);

        final BrokerReportResponse brokerReportResponse = BrokerReportResponse.getDefaultInstance();
        Mockito.doReturn(brokerReportResponse).when(operationsService).getBrokerReportSync(accountId, from, to);

        waitForThrottlingCounters();

        ExecutionUtils.run(() -> operationsService.getPositionsSync(accountId), ApiCallsThrottling.OPERATIONS_SERVICE_LIMIT);

        final Supplier<BrokerReportResponse> supplier = () -> operationsService.getBrokerReportSync(accountId, from, to);
        testThrottling(supplier, ApiCallsThrottling.OPERATIONS_SERVICE_BROKER_REPORT_LIMIT, brokerReportResponse);
    }

    // endregion

    @Test
    void marketDataService() throws InterruptedException {
        final String instrumentId = TestInstruments.APPLE.getFigi();

        GetTradingStatusResponse response = GetTradingStatusResponse.getDefaultInstance();
        Mockito.doReturn(response).when(marketDataService).getTradingStatusSync(instrumentId);

        waitForThrottlingCounters();

        testThrottling(() -> marketDataService.getTradingStatusSync(instrumentId), ApiCallsThrottling.MARKET_DATA_SERVICE_LIMIT, response);
    }

    // region ordersService tests

    @Test
    void ordersService() throws InterruptedException {
        final String accountId = TestAccounts.IIS.getId();
        final String orderId = TestOrderStates.ORDER_STATE1.getOrderId();

        OrderState orderState = OrderState.getDefaultInstance();
        Mockito.doReturn(orderState).when(ordersService).getOrderStateSync(accountId, orderId);

        waitForThrottlingCounters();

        testThrottling(() -> ordersService.getOrderStateSync(accountId, orderId), ApiCallsThrottling.ORDERS_SERVICE_LIMIT, orderState);
    }

    @Test
    void ordersService_getOrdersSync() throws InterruptedException {
        final String accountId = TestAccounts.IIS.getId();
        final String orderId = TestOrderStates.ORDER_STATE1.getOrderId();

        Mockito.doReturn(null).when(ordersService).getOrderStateSync(accountId, orderId);

        List<OrderState> orderStates = List.of(TestOrderStates.ORDER_STATE1.tOrderState(), TestOrderStates.ORDER_STATE2.tOrderState());
        Mockito.doReturn(orderStates).when(ordersService).getOrdersSync(accountId);

        waitForThrottlingCounters();

        ExecutionUtils.run(() -> ordersService.getOrderStateSync(accountId, orderId), ApiCallsThrottling.ORDERS_SERVICE_LIMIT);

        testThrottling(() -> ordersService.getOrdersSync(accountId), ApiCallsThrottling.ORDERS_SERVICE_GET_ORDERS_LIMIT, orderStates);
    }

    @Test
    void ordersService_postOrderSync() throws InterruptedException {
        final String accountId = TestAccounts.IIS.getId();
        final String orderId = TestOrderStates.ORDER_STATE1.getOrderId();
        final String figi = TestInstruments.APPLE.getFigi();
        final int quantity = 1;
        final Quotation price = Quotation.newBuilder().build();
        final OrderDirection orderDirection = OrderDirection.ORDER_DIRECTION_BUY;
        final OrderType orderType = OrderType.ORDER_TYPE_MARKET;

        Mockito.doReturn(null).when(ordersService).getOrderState(accountId, orderId);

        PostOrderResponse response = PostOrderResponse.getDefaultInstance();
        Mockito.doReturn(response).when(ordersService).postOrderSync(figi, quantity, price, orderDirection, accountId, orderType, orderId);

        waitForThrottlingCounters();

        ExecutionUtils.run(() -> ordersService.getOrderState(accountId, orderId), ApiCallsThrottling.ORDERS_SERVICE_LIMIT);

        final Supplier<PostOrderResponse> supplier =
                () -> ordersService.postOrderSync(figi, quantity, price, orderDirection, accountId, orderType, orderId);
        testThrottling(supplier, ApiCallsThrottling.ORDERS_SERVICE_POST_ORDER_LIMIT, response);
    }

    @Test
    void ordersService_cancelOrderSync() throws InterruptedException {
        final String accountId = TestAccounts.IIS.getId();
        final String orderId = TestOrderStates.ORDER_STATE1.getOrderId();

        final Instant instant = Instant.ofEpochMilli(1000);
        Mockito.doReturn(instant).when(ordersService).cancelOrderSync(accountId, orderId);

        waitForThrottlingCounters();

        ExecutionUtils.run(() -> ordersService.getOrderState(accountId, orderId), ApiCallsThrottling.ORDERS_SERVICE_LIMIT);

        testThrottling(() -> ordersService.cancelOrderSync(accountId, orderId), ApiCallsThrottling.ORDERS_SERVICE_CANCEL_ORDER_LIMIT, instant);
    }

    // endregion

    private <T> void testThrottling(final Supplier<T> supplier, final int limit, final T expectedResult) throws InterruptedException {
        System.gc(); // to reduce the chance of the GC start during test which can increase execution time

        ExecutionResult<T> result = ExecutionUtils.get(supplier, limit);
        long duration = result.duration().toMillis();

        Assertions.assertTrue(duration < 100, "Execution expected to take less than 100 ms, but took " + duration + " ms");

        waitForThrottlingCounters();

        result = ExecutionUtils.get(supplier, limit + 1);
        duration = result.duration().toMillis();

        final long interval = apiProperties.throttlingInterval();
        AssertUtils.assertRangeInclusive(interval - 20, (int) (interval * 1.25), duration);

        Assertions.assertSame(expectedResult, result.result());
    }

    // used to finish all throttling counters triggered by mocks and previous tests
    private void waitForThrottlingCounters() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(apiProperties.throttlingInterval());
    }

}