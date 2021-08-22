package ru.obukhov.trader.test.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TestUtils {

    public static long runAndGetElapsedMillis(final Runnable runnable) {
        final long start = System.currentTimeMillis();
        runnable.run();
        return System.currentTimeMillis() - start;
    }

}