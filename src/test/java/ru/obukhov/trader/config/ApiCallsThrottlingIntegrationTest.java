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
import ru.obukhov.trader.common.util.ExecutionUtils;
import ru.obukhov.trader.config.properties.ApiProperties;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.instrument.TestInstruments;
import ru.obukhov.trader.test.utils.model.order_state.TestOrderStates;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.OrdersService;
import ru.tinkoff.piapi.core.UsersService;

import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

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
        final Instrument instrument = TestInstruments.APPLE.tinkoffInstrument();
        final String figi = instrument.getFigi();

        Mockito.doReturn(instrument).when(instrumentsService).getInstrumentByFigiSync(figi);

        waitForThrottlingCounters();

        testThrottling(() -> instrumentsService.getInstrumentByFigiSync(figi), 200);
    }

    @Test
    void usersService() throws InterruptedException {
        Mockito.doReturn(Collections.emptyList()).when(usersService).getAccountsSync();

        waitForThrottlingCounters();

        testThrottling(() -> usersService.getAccountsSync(), 100);
    }

    // region operationsService tests

    @Test
    void operationsService() throws InterruptedException {
        final String accountId = TestAccounts.IIS.getId();
        final Instant from = Instant.now();
        final Instant to = Instant.now();

        Mockito.doReturn(null).when(operationsService).getPositionsSync(accountId);
        Mockito.doReturn(null).when(operationsService).getBrokerReportSync(accountId, from, to);

        waitForThrottlingCounters();

        final Runnable runnable = () -> operationsService.getBrokerReportSync(accountId, from, to);
        ExecutionUtils.run(runnable, ApiCallsThrottling.OPERATIONS_SERVICE_BROKER_REPORT_LIMIT);

        testThrottling(() -> operationsService.getPositionsSync(accountId), ApiCallsThrottling.OPERATIONS_SERVICE_LIMIT);
    }

    @Test
    void operationsService_getBrokerReportSync() throws InterruptedException {
        final String accountId = TestAccounts.IIS.getId();
        final Instant from = Instant.now();
        final Instant to = Instant.now();

        Mockito.doReturn(null).when(operationsService).getPositionsSync(accountId);
        Mockito.doReturn(null).when(operationsService).getBrokerReportSync(accountId, from, to);

        waitForThrottlingCounters();

        ExecutionUtils.run(() -> operationsService.getPositionsSync(accountId), ApiCallsThrottling.OPERATIONS_SERVICE_LIMIT);

        final Runnable runnable = () -> operationsService.getBrokerReportSync(accountId, from, to);
        testThrottling(runnable, ApiCallsThrottling.OPERATIONS_SERVICE_BROKER_REPORT_LIMIT);
    }

    // endregion

    @Test
    void marketDataService() throws InterruptedException {
        final String instrumentId = TestInstruments.APPLE.getFigi();

        Mockito.doReturn(null).when(marketDataService).getTradingStatusSync(instrumentId);

        waitForThrottlingCounters();

        testThrottling(() -> marketDataService.getTradingStatusSync(instrumentId), ApiCallsThrottling.MARKET_DATA_SERVICE_LIMIT);
    }

    // region ordersService tests

    @Test
    void ordersService() throws InterruptedException {
        final String accountId = TestAccounts.IIS.getId();
        final String orderId = TestOrderStates.ORDER_STATE1.getOrderId();

        Mockito.doReturn(null).when(ordersService).getOrderStateSync(accountId, orderId);

        waitForThrottlingCounters();

        testThrottling(() -> ordersService.getOrderStateSync(accountId, orderId), ApiCallsThrottling.ORDERS_SERVICE_LIMIT);
    }

    @Test
    void ordersService_getOrdersSync() throws InterruptedException {
        final String accountId = TestAccounts.IIS.getId();
        final String orderId = TestOrderStates.ORDER_STATE1.getOrderId();

        Mockito.doReturn(null).when(ordersService).getOrderStateSync(accountId, orderId);
        Mockito.doReturn(null).when(ordersService).getOrdersSync(accountId);

        waitForThrottlingCounters();

        ExecutionUtils.run(() -> ordersService.getOrderStateSync(accountId, orderId), ApiCallsThrottling.ORDERS_SERVICE_LIMIT);

        testThrottling(() -> ordersService.getOrdersSync(accountId), ApiCallsThrottling.ORDERS_SERVICE_GET_ORDERS_LIMIT);
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
        Mockito.doReturn(null).when(ordersService).postOrderSync(figi, quantity, price, orderDirection, accountId, orderType, orderId);

        waitForThrottlingCounters();

        ExecutionUtils.run(() -> ordersService.getOrderState(accountId, orderId), ApiCallsThrottling.ORDERS_SERVICE_LIMIT);

        final Runnable runnable = () -> ordersService.postOrderSync(figi, quantity, price, orderDirection, accountId, orderType, orderId);
        testThrottling(runnable, ApiCallsThrottling.ORDERS_SERVICE_POST_ORDER_LIMIT);
    }

    @Test
    void ordersService_cancelOrderSync() throws InterruptedException {
        final String accountId = TestAccounts.IIS.getId();
        final String orderId = TestOrderStates.ORDER_STATE1.getOrderId();

        Mockito.doReturn(null).when(ordersService).cancelOrderSync(accountId, orderId);

        waitForThrottlingCounters();

        ExecutionUtils.run(() -> ordersService.getOrderState(accountId, orderId), ApiCallsThrottling.ORDERS_SERVICE_LIMIT);

        testThrottling(() -> ordersService.cancelOrderSync(accountId, orderId), ApiCallsThrottling.ORDERS_SERVICE_CANCEL_ORDER_LIMIT);
    }

    // endregion

    private void testThrottling(final Runnable runnable, final int limit) throws InterruptedException {
        System.gc(); // to reduce the chance of the GC start during test which can increase execution time

        long duration = ExecutionUtils.run(runnable, limit).toMillis();

        Assertions.assertTrue(duration < 100, "Execution expected to take less than 100 ms, but took " + duration + " ms");

        waitForThrottlingCounters();

        duration = ExecutionUtils.run(runnable, limit + 1).toMillis();

        final long interval = apiProperties.throttlingInterval();
        AssertUtils.assertRangeInclusive(interval - 20, (int) (interval * 1.25), duration);
    }

    // used to finish all throttling counters triggered by mocks and previous tests
    private void waitForThrottlingCounters() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(apiProperties.throttlingInterval());
    }

}