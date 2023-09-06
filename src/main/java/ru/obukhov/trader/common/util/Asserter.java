package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class Asserter {

    public static void notNull(final Object object, Supplier<? extends RuntimeException> exceptionSupplier) {
        if (object == null) {
            throw exceptionSupplier.get();
        }
    }

}