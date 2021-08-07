package ru.obukhov.trader.web;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.grafana.model.GetDataRequest;

/**
 * Aspect updating certain dateTimes in incoming HTTP requests. Update consists in setting of {@link DateUtils#DEFAULT_OFFSET} with same instant.
 */
@Aspect
@Component
@SuppressWarnings("unused")
public class DefaultTimeZoneAspect {

    @Before("execution(public * ru.obukhov.trader.web.controller.GrafanaController.getData(..))")
    public void beforeGrafanaControllerGetData(JoinPoint joinPoint) {
        final GetDataRequest request = (GetDataRequest) joinPoint.getArgs()[0];
        request.setRange(request.getRange().withDefaultOffsetSameInstant());
    }

}