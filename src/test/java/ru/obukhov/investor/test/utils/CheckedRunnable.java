package ru.obukhov.investor.test.utils;

@FunctionalInterface
public interface CheckedRunnable {
    void run() throws Exception;
}