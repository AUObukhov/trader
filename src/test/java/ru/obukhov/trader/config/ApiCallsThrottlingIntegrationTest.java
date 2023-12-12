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
import ru.obukhov.trader.test.utils.model.orderstate.TestOrderStates;
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

        resetThrottlingCounters();

        testThrottling(() -> instrumentsService.getInstrumentByFigiSync(figi), 200);
    }

    @Test
    void usersService() throws InterruptedException {
        Mockito.doReturn(Collections.emptyList()).when(usersService).getAccountsSync();

        resetThrottlingCounters();

        testThrottling(() -> usersService.getAccountsSync(), 100);
    }

    // region operationsService tests

    @Test
    void operationsService() throws InterruptedException {
        final String accountId = TestAccounts.IIS.account().id();

        Mockito.doReturn(null).when(operationsService).getPositionsSync(accountId);

        resetThrottlingCounters();

        testThrottling(() -> operationsService.getPositionsSync(accountId), 200);
    }

    @Test
    void operationsService_getBrokerReport() throws InterruptedException {
        final String accountId = TestAccounts.IIS.account().id();
        final Instant from = Instant.now();
        final Instant to = Instant.now();

        Mockito.doReturn(null).when(operationsService).getPositionsSync(accountId);
        Mockito.doReturn(null).when(operationsService).getBrokerReportSync(accountId, from, to);

        resetThrottlingCounters();

        ExecutionUtils.run(() -> operationsService.getPositionsSync(accountId), 200);

        testThrottling(() -> operationsService.getBrokerReportSync(accountId, from, to), 5);
    }

    // endregion

    @Test
    void marketDataService() throws InterruptedException {
        final String instrumentId = TestInstruments.APPLE.instrument().figi();

        Mockito.doReturn(null).when(marketDataService).getTradingStatusSync(instrumentId);

        resetThrottlingCounters();

        testThrottling(() -> marketDataService.getTradingStatusSync(instrumentId), 300);
    }

    // region ordersService tests

    @Test
    void ordersService() throws InterruptedException {
        final String accountId = TestAccounts.IIS.account().id();
        final String orderId = TestOrderStates.ORDER_STATE1.orderState().orderId();

        Mockito.doReturn(null).when(ordersService).getOrderStateSync(accountId, orderId);

        resetThrottlingCounters();

        testThrottling(() -> ordersService.getOrderStateSync(accountId, orderId), 100);
    }

    @Test
    void ordersService_getOrders() throws InterruptedException {
        final String accountId = TestAccounts.IIS.account().id();
        final String orderId = TestOrderStates.ORDER_STATE1.orderState().orderId();

        Mockito.doReturn(null).when(ordersService).getOrderStateSync(accountId, orderId);
        Mockito.doReturn(null).when(ordersService).getOrdersSync(accountId);

        resetThrottlingCounters();

        ExecutionUtils.run(() -> ordersService.getOrderStateSync(accountId, orderId), 100);

        testThrottling(() -> ordersService.getOrdersSync(accountId), 200);
    }

    @Test
    void ordersService_postOrders() throws InterruptedException {
        final String accountId = TestAccounts.IIS.account().id();
        final String orderId = TestOrderStates.ORDER_STATE1.orderState().orderId();
        final String figi = TestInstruments.APPLE.instrument().figi();
        final int quantity = 1;
        final Quotation price = Quotation.newBuilder().build();
        final OrderDirection orderDirection = OrderDirection.ORDER_DIRECTION_BUY;
        final OrderType orderType = OrderType.ORDER_TYPE_MARKET;

        Mockito.doReturn(null).when(ordersService).getOrderState(accountId, orderId);
        Mockito.doReturn(null).when(ordersService).postOrderSync(figi, quantity, price, orderDirection, accountId, orderType, orderId);

        resetThrottlingCounters();

        ExecutionUtils.run(() -> ordersService.getOrderState(accountId, orderId), 100);

        testThrottling(() -> ordersService.postOrderSync(figi, quantity, price, orderDirection, accountId, orderType, orderId), 300);
    }

    @Test
    void ordersService_cancelOrder() throws InterruptedException {
        final String accountId = TestAccounts.IIS.account().id();
        final String orderId = TestOrderStates.ORDER_STATE1.orderState().orderId();

        Mockito.doReturn(null).when(ordersService).cancelOrderSync(accountId, orderId);

        resetThrottlingCounters();

        ExecutionUtils.run(() -> ordersService.getOrderState(accountId, orderId), 100);

        testThrottling(() -> ordersService.cancelOrderSync(accountId, orderId), 100);
    }

    // endregion

    private void testThrottling(final Runnable runnable, final int limit) throws InterruptedException {
        System.gc(); // to reduce the chance of the GC start during test which can increase execution time

        long duration = ExecutionUtils.run(runnable, limit + 1).toMillis();

        final Integer interval = apiProperties.throttlingInterval();
        AssertUtils.assertRangeInclusive(interval - 20, (int) (interval * 1.25), duration);

        resetThrottlingCounters();

        duration = ExecutionUtils.run(runnable, limit).toMillis();

        String message = "Execution expected to take less than 100 ms, but took " + duration + " ms";
        Assertions.assertTrue(duration < 100, message);
    }

    // used to finish all throttling counters triggered by mocks and previous tests
    private void resetThrottlingCounters() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(apiProperties.throttlingInterval());
    }

}