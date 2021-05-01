package ru.obukhov.trader.test.utils;

public class TestUtils {

    public static long runAndGetElapsedMillis(Runnable runnable) {
        long start = System.currentTimeMillis();
        runnable.run();
        return System.currentTimeMillis() - start;
    }

}