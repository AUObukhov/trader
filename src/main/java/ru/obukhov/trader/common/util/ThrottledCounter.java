package ru.obukhov.trader.common.util;

import lombok.SneakyThrows;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Synchronized counter, incremented from outside,
 * each increment is followed by a decrement after given delay in milliseconds
 * When value is maximum, current thread starts waiting until value decrement
 */
public class ThrottledCounter {

    private final CounterWithMaxValue counterWithMaxValue;
    private final Timer timer = new Timer();
    private final long interval;

    /**
     * Creates new instance of {@link ThrottledCounter}
     *
     * @param interval delay before decrement after increment in milliseconds
     * @param maxValue maximum value of counter
     */
    public ThrottledCounter(long interval, int maxValue) {
        this.interval = interval;
        this.counterWithMaxValue = new CounterWithMaxValue(maxValue);
    }

    /**
     * Increments current value and starts task to decrement it after delay
     */
    @SneakyThrows
    public synchronized void increment() {
        this.counterWithMaxValue.increment();

        timer.schedule(new DecrementTask(), interval);
    }

    public int getValue() {
        return this.counterWithMaxValue.getValue();
    }

    private static class CounterWithMaxValue {
        private final AtomicInteger value = new AtomicInteger(0);

        int maxValue;

        private CounterWithMaxValue(int maxValue) {
            this.maxValue = maxValue;
        }

        @SneakyThrows
        private synchronized void increment() {
            while (value.get() >= maxValue) {
                wait();
            }

            value.incrementAndGet();
        }

        private synchronized void decrement() {
            int previousValue = value.getAndDecrement();
            if (previousValue == maxValue) {
                notifyAll();
            }
        }

        private int getValue() {
            return this.value.get();
        }
    }

    private class DecrementTask extends TimerTask {
        @Override
        public synchronized void run() {
            counterWithMaxValue.decrement();
        }
    }

}