package ru.obukhov.trader.web;

import lombok.Getter;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.JoinPoint;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.grafana.model.GetDataRequest;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.web.model.exchange.GetCandlesRequest;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

class DefaultTimeZoneAspectUnitTest {

    @SuppressWarnings("unused")
    static Stream<Arguments> getData() {
        return Stream.of(
                Arguments.of(null, null, null, null),
                Arguments.of(
                        null,
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, ZoneOffset.UTC),
                        null,
                        DateTimeTestData.createDateTime(2020, 10, 5, 13)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, ZoneOffset.UTC),
                        null,
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        null
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, ZoneOffset.UTC),
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, ZoneOffset.UTC),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData")
    void beforeGrafanaControllerGetData(
            final OffsetDateTime from,
            final OffsetDateTime to,
            final OffsetDateTime expectedFrom,
            final OffsetDateTime expectedTo
    ) {
        final DefaultTimeZoneAspect aspect = new DefaultTimeZoneAspect();

        final Interval interval = Interval.of(from, to);
        final GetDataRequest request = new GetDataRequest().setInterval(interval);
        final Object[] arguments = new Object[]{request};
        final ProxyMethodInvocation methodInvocation = new TestMethodInvocation(arguments);
        final JoinPoint joinPoint = new MethodInvocationProceedingJoinPoint(methodInvocation);

        aspect.beforeGrafanaControllerGetData(joinPoint);

        Assertions.assertEquals(expectedFrom, request.getInterval().getFrom());
        Assertions.assertEquals(expectedTo, request.getInterval().getTo());
    }

    @ParameterizedTest
    @MethodSource("getData")
    void beforeStatisticsControllerGetCandles(
            final OffsetDateTime from,
            final OffsetDateTime to,
            final OffsetDateTime expectedFrom,
            final OffsetDateTime expectedTo
    ) {
        final DefaultTimeZoneAspect aspect = new DefaultTimeZoneAspect();

        final Interval interval = Interval.of(from, to);
        final GetCandlesRequest request = new GetCandlesRequest();
        request.setTicker(TestShare1.TICKER);
        request.setInterval(interval);
        request.setCandleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN);
        request.setMovingAverageType(MovingAverageType.SIMPLE);
        request.setSmallWindow(50);
        request.setBigWindow(200);
        request.setSaveToFile(false);

        final Object[] arguments = new Object[]{request};
        final ProxyMethodInvocation methodInvocation = new TestMethodInvocation(arguments);
        final JoinPoint joinPoint = new MethodInvocationProceedingJoinPoint(methodInvocation);

        aspect.beforeStatisticsControllerGetCandles(joinPoint);

        Assertions.assertEquals(expectedFrom, request.getInterval().getFrom());
        Assertions.assertEquals(expectedTo, request.getInterval().getTo());
    }

    @Getter
    @SuppressWarnings("all")
    private static class TestMethodInvocation implements ProxyMethodInvocation {

        private final Object[] arguments;

        public TestMethodInvocation(Object[] arguments) {
            this.arguments = arguments;
        }

        @NotNull
        @Override
        public Object getProxy() {
            return null;
        }

        @NotNull
        @Override
        public MethodInvocation invocableClone() {
            return null;
        }

        @NotNull
        @Override
        public MethodInvocation invocableClone(@NotNull Object... arguments) {
            return null;
        }

        @Override
        public void setArguments(@NotNull Object... arguments) {
        }

        @Override
        public void setUserAttribute(@NotNull String key, Object value) {
        }

        @Override
        public Object getUserAttribute(@NotNull String key) {
            return null;
        }

        @NotNull
        @Override
        public Method getMethod() {
            return null;
        }

        @Override
        public Object proceed() throws Throwable {
            return null;
        }

        @Override
        public Object getThis() {
            return null;
        }

        @NotNull
        @Override
        public AccessibleObject getStaticPart() {
            return null;
        }
    }

}