package ru.obukhov.trader.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
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
        final Integer interval = apiProperties.throttlingInterval();

        this.instrumentServiceCounter = new ThrottledCounter(200, interval);
        this.usersServiceCounter = new ThrottledCounter(100, interval);
        this.operationsServiceCounter = new ThrottledCounter(200, interval);
        this.operationsServiceGetBrokerReportCounter = new ThrottledCounter(5, interval);
        this.marketDataServiceCounter = new ThrottledCounter(300, interval);
        this.ordersServiceCounter = new ThrottledCounter(100, interval);
        this.ordersServiceGetOrdersCounter = new ThrottledCounter(200, interval);
        this.ordersServicePostOrderCounter = new ThrottledCounter(300, interval);
        this.ordersServiceCancelOrderCounter = new ThrottledCounter(100, interval);
    }

    @Before("within(ru.tinkoff.piapi.core.InstrumentsService)")
    public void throttleInstrumentService() {
        increment(instrumentServiceCounter, "InstrumentService");
    }

    @Before("within(ru.tinkoff.piapi.core.UsersService)")
    public void throttleUsersService() {
        increment(usersServiceCounter, "UsersService");
    }

    // region OperationsService throttling

    @Before("within(ru.tinkoff.piapi.core.OperationsService)" +
            " && !execution(* ru.tinkoff.piapi.core.OperationsService.getBrokerReport*(..))")
    public void throttleOperationsService() {
        increment(operationsServiceCounter, "OperationsService");
    }

    @Before("execution(* ru.tinkoff.piapi.core.OperationsService.getBrokerReport*(..))")
    public void throttleOperationsServiceGetBrokerReport() {
        increment(operationsServiceGetBrokerReportCounter, "OperationsService.getBrokerReport*");
    }

    // endregion

    @Before("within(ru.tinkoff.piapi.core.MarketDataService)")
    public void throttleMarketDataService() {
        increment(marketDataServiceCounter, "MarketDataService");
    }

    // region OrdersService throttling

    @Before("within(ru.tinkoff.piapi.core.OrdersService)"
            + " && !execution(* ru.tinkoff.piapi.core.OrdersService.getOrders*(..))"
            + " && !execution(* ru.tinkoff.piapi.core.OrdersService.postOrder*(..))"
            + " && !execution(* ru.tinkoff.piapi.core.OrdersService.cancelOrder*(..)))"
    )
    public void throttleOrdersService(JoinPoint joinPoint) {
        increment(ordersServiceCounter, "OrdersService");
    }

    @Before("execution(* ru.tinkoff.piapi.core.OrdersService.getOrders*(..))")
    public void throttleOrdersServiceGetOrders() {
        increment(ordersServiceGetOrdersCounter, "OrdersService.getOrders*");
    }

    @Before("execution(* ru.tinkoff.piapi.core.OrdersService.postOrder*(..))")
    public void throttleOrdersServicePostOrder() {
        increment(ordersServicePostOrderCounter, "OrdersService.postOrder*");
    }

    @Before("execution(* ru.tinkoff.piapi.core.OrdersService.cancelOrder*(..))")
    public void throttleOrdersServiceCancelOrder() {
        increment(ordersServiceCancelOrderCounter, "OrdersService.cancelOrder*");
    }

    // endregion

    private static void increment(final ThrottledCounter counter, final String targetName) {
        log.trace("{} throttling start. Counter = {}", targetName, counter.getValue());
        counter.increment();
        log.trace("{} throttling end. Counter = {}", targetName, counter.getValue());
    }

}