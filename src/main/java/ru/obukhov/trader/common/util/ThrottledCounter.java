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
    private final long interval;
    private final Timer timer;

    /**
     * Creates new instance of {@link ThrottledCounter}
     *
     * @param maxValue maximum value of counter
     * @param interval delay before decrement after increment in milliseconds
     */
    public ThrottledCounter(final int maxValue, final long interval) {
        this.counterWithMaxValue = new CounterWithMaxValue(maxValue);
        this.interval = interval;
        this.timer = new Timer();
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

        private CounterWithMaxValue(final int maxValue) {
            this.maxValue = maxValue;
        }

        private synchronized void increment() {
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
        public void run() {
            counterWithMaxValue.decrement();
        }
    }

}