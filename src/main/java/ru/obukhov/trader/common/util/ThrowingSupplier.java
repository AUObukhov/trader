package ru.obukhov.trader.common.util;

@FunctionalInterface
public interface ThrowingSupplier<T> {
    T get() throws Exception;
}