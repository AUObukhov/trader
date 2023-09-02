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
import ru.obukhov.trader.test.utils.model.account.TestAccount1;
import ru.obukhov.trader.test.utils.model.instrument.TestInstrument1;
import ru.obukhov.trader.test.utils.model.orderstate.TestOrderState1;
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
        final String figi = TestInstrument1.FIGI;

        Mockito.doReturn(TestInstrument1.TINKOFF_INSTRUMENT).when(instrumentsService).getInstrumentByFigiSync(figi);

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
        final String accountId = TestAccount1.ID;

        Mockito.doReturn(null).when(operationsService).getPositionsSync(accountId);

        resetThrottlingCounters();

        testThrottling(() -> operationsService.getPositionsSync(accountId), 200);
    }

    @Test
    void operationsService_getBrokerReport() throws InterruptedException {
        final String accountId = TestAccount1.ID;
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
        final String instrumentId = TestInstrument1.FIGI;

        Mockito.doReturn(null).when(marketDataService).getTradingStatusSync(instrumentId);

        resetThrottlingCounters();

        testThrottling(() -> marketDataService.getTradingStatusSync(instrumentId), 300);
    }

    // region ordersService tests

    @Test
    void ordersService() throws InterruptedException {
        final String accountId = TestAccount1.ID;
        final String orderId = TestOrderState1.ORDER_ID;

        Mockito.doReturn(null).when(ordersService).getOrderStateSync(accountId, orderId);

        resetThrottlingCounters();

        testThrottling(() -> ordersService.getOrderStateSync(accountId, orderId), 100);
    }

    @Test
    void ordersService_getOrders() throws InterruptedException {
        final String accountId = TestAccount1.ID;
        final String orderId = TestOrderState1.ORDER_ID;

        Mockito.doReturn(null).when(ordersService).getOrderStateSync(TestAccount1.ID, orderId);
        Mockito.doReturn(null).when(ordersService).getOrdersSync(accountId);

        resetThrottlingCounters();

        ExecutionUtils.run(() -> ordersService.getOrderStateSync(TestAccount1.ID, orderId), 100);

        testThrottling(() -> ordersService.getOrdersSync(accountId), 200);
    }

    @Test
    void ordersService_postOrders() throws InterruptedException {
        final String accountId = TestAccount1.ID;
        final String orderId = TestOrderState1.ORDER_ID;
        final String figi = TestInstrument1.FIGI;
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
        final String accountId = TestAccount1.ID;
        final String orderId = TestOrderState1.ORDER_ID;

        Mockito.doReturn(null).when(ordersService).cancelOrderSync(accountId, orderId);

        resetThrottlingCounters();

        ExecutionUtils.run(() -> ordersService.getOrderState(accountId, orderId), 100);

        testThrottling(() -> ordersService.cancelOrderSync(accountId, orderId), 100);
    }

    // endregion

    private void testThrottling(final Runnable runnable, final int limit) throws InterruptedException {
        long duration = ExecutionUtils.run(runnable, limit + 1).toMillis();

        final Integer interval = apiProperties.throttlingInterval();
        AssertUtils.assertRangeInclusive(interval - 20, interval + 20, duration);

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