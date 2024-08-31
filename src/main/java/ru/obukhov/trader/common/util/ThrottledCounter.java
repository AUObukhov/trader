package ru.obukhov.trader.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Synchronized counter, incremented from outside.
 * Each increment is followed by a decrement after given delay in milliseconds.
 * If value is maximum, calling thread stop to wait until next decrement.
 */
@Slf4j
public class ThrottledCounter {

    private final CounterWithMaxValue counterWithMaxValue;
    private final Timer timer;

    public ThrottledCounter(final int maxValue) {
        this.counterWithMaxValue = new CounterWithMaxValue(maxValue);
        this.timer = new Timer();
    }

    public void increment() {
        this.counterWithMaxValue.increment();
    }

    public void scheduleDecrement(final long interval) {
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
            } catch (final InterruptedException exception) {
                log.error("Counter increment waiting was interrupted", exception);
                Thread.currentThread().interrupt();
            }
        }

        private synchronized void decrement() {
            if (value.getAndDecrement() == maxValue) {
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