package ru.obukhov.trader.common.util;

@FunctionalInterface
public interface ThrowingSupplier<T> {
    @SuppressWarnings("java:S112")
        // Generic exceptions should never be thrown
    T get() throws Exception;
}