package ru.obukhov.trader.web;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.grafana.model.GetDataRequest;
import ru.obukhov.trader.web.model.exchange.GetCandlesRequest;

/**
 * Aspect updating certain dateTimes in incoming HTTP requests. Update consists in setting of {@link DateUtils#DEFAULT_OFFSET} with same instant.
 */
@Aspect
@Component
public class DefaultTimeZoneAspect {

    @Before("execution(public * ru.obukhov.trader.web.controller.GrafanaController.getData(..))")
    public void beforeGrafanaControllerGetData(JoinPoint joinPoint) {
        final GetDataRequest request = (GetDataRequest) joinPoint.getArgs()[0];
        request.setInterval(request.getInterval().withDefaultOffsetSameInstant());
    }

    @Before("execution(public * ru.obukhov.trader.web.controller.StatisticsController.getCandles(..))")
    public void beforeStatisticsControllerGetCandles(JoinPoint joinPoint) {
        final GetCandlesRequest request = (GetCandlesRequest) joinPoint.getArgs()[0];
        request.setInterval(request.getInterval().withDefaultOffsetSameInstant());
    }

}