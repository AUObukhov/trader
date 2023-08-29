package ru.obukhov.trader.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import ru.obukhov.trader.common.util.ThrottledCounter;

/**
 * Aspect to limit rate of API calls according to Tinkoff's limits
 *
 * @see <a href=https://tinkoff.github.io/investAPI/limits/">Tinkoff documentation</a>
 */
@Slf4j
@Aspect
@Component
@EnableAspectJAutoProxy
@SuppressWarnings("unused")
public class ApiCallsThrottling {

    static final int INTERVAL = 60_000;

    private static final ThrottledCounter INSTRUMENT_SERVICE_COUNTER = new ThrottledCounter(200, INTERVAL);
    private static final ThrottledCounter USERS_SERVICE_COUNTER = new ThrottledCounter(100, INTERVAL);
    private static final ThrottledCounter OPERATIONS_SERVICE_COUNTER = new ThrottledCounter(200, INTERVAL);
    private static final ThrottledCounter OPERATIONS_SERVICE_GET_BROKER_REPORT_COUNTER = new ThrottledCounter(5, INTERVAL);
    private static final ThrottledCounter MARKET_DATA_SERVICE_COUNTER = new ThrottledCounter(300, INTERVAL);
    private static final ThrottledCounter ORDERS_SERVICE_COUNTER = new ThrottledCounter(100, INTERVAL);
    private static final ThrottledCounter ORDERS_SERVICE_GET_ORDERS_COUNTER = new ThrottledCounter(200, INTERVAL);
    private static final ThrottledCounter ORDERS_SERVICE_POST_ORDER_COUNTER = new ThrottledCounter(300, INTERVAL);
    private static final ThrottledCounter ORDERS_SERVICE_CANCEL_ORDER_COUNTER = new ThrottledCounter(100, INTERVAL);


    @Before("within(ru.tinkoff.piapi.core.InstrumentsService)")
    public void throttleInstrumentService() {
        increment(INSTRUMENT_SERVICE_COUNTER, "InstrumentService");
    }

    @Before("within(ru.tinkoff.piapi.core.UsersService)")
    public void throttleUsersService() {
        increment(USERS_SERVICE_COUNTER, "UsersService");
    }

    // region OperationsService throttling

    @Before("within(ru.tinkoff.piapi.core.OperationsService)" +
            " && !execution(* ru.tinkoff.piapi.core.OperationsService.getBrokerReport*(..))")
    public void throttleOperationsService() {
        increment(OPERATIONS_SERVICE_COUNTER, "OperationsService");
    }

    @Before("execution(* ru.tinkoff.piapi.core.OperationsService.getBrokerReport*(..))")
    public void throttleOperationsServiceGetBrokerReport() {
        increment(OPERATIONS_SERVICE_GET_BROKER_REPORT_COUNTER, "OperationsService.getBrokerReport*");
    }

    // endregion

    @Before("within(ru.tinkoff.piapi.core.MarketDataService)")
    public void throttleMarketDataService() {
        increment(MARKET_DATA_SERVICE_COUNTER, "MarketDataService");
    }

    // region OrdersService throttling

    @Before("within(ru.tinkoff.piapi.core.OrdersService)"
            + " && !execution(* ru.tinkoff.piapi.core.OrdersService.getOrders*(..))"
            + " && !execution(* ru.tinkoff.piapi.core.OrdersService.postOrder*(..))"
            + " && !execution(* ru.tinkoff.piapi.core.OrdersService.cancelOrder*(..)))"
    )
    public void throttleOrdersService(JoinPoint joinPoint) {
        increment(ORDERS_SERVICE_COUNTER, "OrdersService");
    }

    @Before("execution(* ru.tinkoff.piapi.core.OrdersService.getOrders*(..))")
    public void throttleOrdersServiceGetOrders() {
        increment(ORDERS_SERVICE_GET_ORDERS_COUNTER, "OrdersService.getOrders*");
    }

    @Before("execution(* ru.tinkoff.piapi.core.OrdersService.postOrder*(..))")
    public void throttleOrdersServicePostOrder() {
        increment(ORDERS_SERVICE_POST_ORDER_COUNTER, "OrdersService.postOrder*");
    }

    @Before("execution(* ru.tinkoff.piapi.core.OrdersService.cancelOrder*(..))")
    public void throttleOrdersServiceCancelOrder() {
        increment(ORDERS_SERVICE_CANCEL_ORDER_COUNTER, "OrdersService.cancelOrder*");
    }

    // endregion

    private static void increment(final ThrottledCounter counter, final String targetName) {
        log.trace("{} throttling start. Counter = {}", targetName, counter.getValue());
        counter.increment();
        log.trace("{} throttling end. Counter = {}", targetName, counter.getValue());
    }

}