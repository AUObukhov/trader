package ru.obukhov.trader.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import ru.obukhov.trader.common.util.ThrottledCounter;
import ru.obukhov.trader.config.properties.ApiProperties;

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

    static final int INSTRUMENT_SERVICE_LIMIT = 200;
    static final int USER_SERVICE_LIMIT = 100;
    static final int OPERATIONS_SERVICE_LIMIT = 200;
    static final int OPERATIONS_SERVICE_BROKER_REPORT_LIMIT = 5;
    static final int MARKET_DATA_SERVICE_LIMIT = 300;
    static final int ORDERS_SERVICE_LIMIT = 100;
    static final int ORDERS_SERVICE_GET_ORDERS_LIMIT = 200;
    static final int ORDERS_SERVICE_POST_ORDER_LIMIT = 300;
    static final int ORDERS_SERVICE_CANCEL_ORDER_LIMIT = 100;

    private final long interval;
    private final ThrottledCounter instrumentServiceCounter;
    private final ThrottledCounter usersServiceCounter;
    private final ThrottledCounter operationsServiceCounter;
    private final ThrottledCounter operationsServiceGetBrokerReportCounter;
    private final ThrottledCounter marketDataServiceCounter;
    private final ThrottledCounter ordersServiceCounter;
    private final ThrottledCounter ordersServiceGetOrdersCounter;
    private final ThrottledCounter ordersServicePostOrderCounter;
    private final ThrottledCounter ordersServiceCancelOrderCounter;

    public ApiCallsThrottling(final ApiProperties apiProperties) {
        interval = apiProperties.throttlingInterval();

        this.instrumentServiceCounter = new ThrottledCounter(INSTRUMENT_SERVICE_LIMIT);
        this.usersServiceCounter = new ThrottledCounter(USER_SERVICE_LIMIT);
        this.operationsServiceCounter = new ThrottledCounter(OPERATIONS_SERVICE_LIMIT);
        this.operationsServiceGetBrokerReportCounter = new ThrottledCounter(OPERATIONS_SERVICE_BROKER_REPORT_LIMIT);
        this.marketDataServiceCounter = new ThrottledCounter(MARKET_DATA_SERVICE_LIMIT);
        this.ordersServiceCounter = new ThrottledCounter(ORDERS_SERVICE_LIMIT);
        this.ordersServiceGetOrdersCounter = new ThrottledCounter(ORDERS_SERVICE_GET_ORDERS_LIMIT);
        this.ordersServicePostOrderCounter = new ThrottledCounter(ORDERS_SERVICE_POST_ORDER_LIMIT);
        this.ordersServiceCancelOrderCounter = new ThrottledCounter(ORDERS_SERVICE_CANCEL_ORDER_LIMIT);
    }

    @Around("within(ru.tinkoff.piapi.core.InstrumentsService)")
    public Object throttleInstrumentService(final ProceedingJoinPoint joinPoint) throws Throwable {
        return throttle(instrumentServiceCounter, "InstrumentService", joinPoint);
    }

    @Around("within(ru.tinkoff.piapi.core.UsersService)")
    public Object throttleUsersService(final ProceedingJoinPoint joinPoint) throws Throwable {
        return throttle(usersServiceCounter, "UsersService", joinPoint);
    }

    // region OperationsService throttling

    @Around("within(ru.tinkoff.piapi.core.OperationsService) && !execution(* ru.tinkoff.piapi.core.OperationsService.getBrokerReport*(..))")
    public Object throttleOperationsService(final ProceedingJoinPoint joinPoint) throws Throwable {
        return throttle(operationsServiceCounter, "OperationsService", joinPoint);
    }

    @Around("execution(* ru.tinkoff.piapi.core.OperationsService.getBrokerReport*(..))")
    public Object throttleOperationsServiceGetBrokerReport(final ProceedingJoinPoint joinPoint) throws Throwable {
        return throttle(operationsServiceGetBrokerReportCounter, "OperationsService.getBrokerReport*", joinPoint);
    }

    // endregion

    @Around("within(ru.tinkoff.piapi.core.MarketDataService)")
    public Object throttleMarketDataService(final ProceedingJoinPoint joinPoint) throws Throwable {
        return throttle(marketDataServiceCounter, "MarketDataService", joinPoint);
    }

    // region OrdersService throttling

    @Around("within(ru.tinkoff.piapi.core.OrdersService)"
            + " && !execution(* ru.tinkoff.piapi.core.OrdersService.getOrders*(..))"
            + " && !execution(* ru.tinkoff.piapi.core.OrdersService.postOrder*(..))"
            + " && !execution(* ru.tinkoff.piapi.core.OrdersService.cancelOrder*(..)))"
    )
    public Object throttleOrdersService(final ProceedingJoinPoint joinPoint) throws Throwable {
        return throttle(ordersServiceCounter, "OrdersService", joinPoint);
    }

    @Around("execution(* ru.tinkoff.piapi.core.OrdersService.getOrders*(..))")
    public Object throttleOrdersServiceGetOrders(final ProceedingJoinPoint joinPoint) throws Throwable {
        return throttle(ordersServiceGetOrdersCounter, "OrdersService.getOrders*", joinPoint);
    }

    @Around("execution(* ru.tinkoff.piapi.core.OrdersService.postOrder*(..))")
    public Object throttleOrdersServicePostOrder(final ProceedingJoinPoint joinPoint) throws Throwable {
        return throttle(ordersServicePostOrderCounter, "OrdersService.postOrder*", joinPoint);
    }

    @Around("execution(* ru.tinkoff.piapi.core.OrdersService.cancelOrder*(..))")
    public Object throttleOrdersServiceCancelOrder(final ProceedingJoinPoint joinPoint) throws Throwable {
        return throttle(ordersServiceCancelOrderCounter, "OrdersService.cancelOrder*", joinPoint);
    }

    // endregion

    public Object throttle(final ThrottledCounter counter, final String targetName, final ProceedingJoinPoint joinPoint)
            throws Throwable {
        log.trace("{} throttling start. Counter = {}", targetName, counter.getValue());
        counter.increment();
        log.trace("{} throttling end. Counter = {}. Proceeding", targetName, counter.getValue());
        final Object result = joinPoint.proceed();
        log.trace("{} executed. Scheduling of counter decrementing", targetName);
        counter.scheduleDecrement(interval);
        return result;
    }

}