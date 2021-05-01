package ru.obukhov.trader.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Synchronized counter, incremented from outside,
 * each increment is followed by a decrement after given delay in milliseconds
 * When value is maximum, current thread starts waiting until value decrement
 */
@Slf4j
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
    public void increment() {
        this.counterWithMaxValue.increment();

        timer.schedule(new DecrementTask(), interval);
    }

    public int getValue() {
        return this.counterWithMaxValue.getValue();
    }

    private static class CounterWithMaxValue {
        private final AtomicInteger value = new AtomicInteger(0);

        private final int maxValue;

        private CounterWithMaxValue(int maxValue) {
            this.maxValue = maxValue;
        }

        private void increment() {
            synchronized (this) {
                try {
                    while (value.get() >= maxValue) {
                        wait();
                    }

                    value.incrementAndGet();
                } catch (InterruptedException exception) {
                    log.error("Counter increment waiting was interrupted", exception);
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void decrement() {
            synchronized (this) {
                int previousValue = value.getAndDecrement();
                if (previousValue == maxValue) {
                    notifyAll();
                }
            }
        }

        private int getValue() {
            return this.value.get();
        }
    }

    private class DecrementTask extends TimerTask {
        @Override
        public void run() {
            synchronized (this) {
                counterWithMaxValue.decrement();
            }
        }
    }

}