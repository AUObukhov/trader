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
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.grafana.model.GetDataRequest;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

class DefaultTimeZoneAspectUnitTest {

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forWithDefaultOffsetSameInstant() {
        return Stream.of(
                Arguments.of(null, null, null, null),
                Arguments.of(
                        null,
                        OffsetDateTime.of(2020, 10, 5, 10, 0, 0, 0, ZoneOffset.UTC),
                        null,
                        DateUtils.getDateTime(2020, 10, 5, 13, 0, 0, 0)
                ),
                Arguments.of(
                        OffsetDateTime.of(2020, 10, 5, 10, 0, 0, 0, ZoneOffset.UTC),
                        null,
                        DateUtils.getDateTime(2020, 10, 5, 13, 0, 0, 0),
                        null
                ),
                Arguments.of(
                        OffsetDateTime.of(2020, 10, 5, 10, 0, 0, 0, ZoneOffset.UTC),
                        OffsetDateTime.of(2020, 10, 5, 10, 0, 0, 0, ZoneOffset.UTC),
                        DateUtils.getDateTime(2020, 10, 5, 13, 0, 0, 0),
                        DateUtils.getDateTime(2020, 10, 5, 13, 0, 0, 0)
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 13, 0, 0, 0),
                        DateUtils.getDateTime(2020, 10, 5, 13, 0, 0, 0),
                        DateUtils.getDateTime(2020, 10, 5, 13, 0, 0, 0),
                        DateUtils.getDateTime(2020, 10, 5, 13, 0, 0, 0)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forWithDefaultOffsetSameInstant")
    void withDefaultOffsetSameInstant(OffsetDateTime from, OffsetDateTime to, OffsetDateTime expectedFrom, OffsetDateTime expectedTo) {
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