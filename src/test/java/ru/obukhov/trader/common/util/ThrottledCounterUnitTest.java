package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

class ThrottledCounterUnitTest {

    @Test
    void decrementsValue_whenIntervalPassed() throws InterruptedException {
        final ThrottledCounter counter = new ThrottledCounter(3, 1000);
        incrementAndScheduleDecrement(counter);
        final int value1 = counter.getValue();

        TimeUnit.MILLISECONDS.sleep(500);
        final int value2 = counter.getValue();

        incrementAndScheduleDecrement(counter);
        incrementAndScheduleDecrement(counter);
        final int value3 = counter.getValue();

        TimeUnit.MILLISECONDS.sleep(750);
        final int value4 = counter.getValue();

        TimeUnit.MILLISECONDS.sleep(500);
        final int value5 = counter.getValue();

        Assertions.assertEquals(1, value1);
        Assertions.assertEquals(1, value2);
        Assertions.assertEquals(3, value3);
        Assertions.assertEquals(2, value4);
        Assertions.assertEquals(0, value5);
    }

    @Test
    void waiting_whenValueIsMax() throws InterruptedException {
        final ThrottledCounter counter = new ThrottledCounter(3, 1000);
        final long start = System.currentTimeMillis();

        final Duration elapsed1 = ExecutionUtils.run(() -> incrementAndScheduleDecrement(counter));
        final int value1 = counter.getValue();

        TimeUnit.MILLISECONDS.sleep(250); // not expected to decrement during this period
        final Duration elapsed2 = ExecutionUtils.run(() -> incrementAndScheduleDecrement(counter));
        final int value2 = counter.getValue();

        TimeUnit.MILLISECONDS.sleep(250); // not expected to decrement during this period
        final Duration elapsed3 = ExecutionUtils.run(() -> incrementAndScheduleDecrement(counter));
        final int value3 = counter.getValue();

        // expected to wait for ~500 milliseconds until first decrement, then counter::increment expected to be executed
        final long elapsed4 = ExecutionUtils.run(() -> incrementAndScheduleDecrement(counter)).toMillis();
        final int value4 = counter.getValue();

        TimeUnit.MILLISECONDS.sleep(50); // overhead buffer
        TimeUnit.MILLISECONDS.sleep(250); // second decrement expected during this period
        final int value5 = counter.getValue();

        TimeUnit.MILLISECONDS.sleep(250); // third decrement expected during this period
        final int value6 = counter.getValue();

        TimeUnit.MILLISECONDS.sleep(500); // fourth decrement expected during this period
        final int value7 = counter.getValue();

        final long elapsedOverall = System.currentTimeMillis() - start;

        Assertions.assertTrue(elapsed1.toMillis() <= 1);
        Assertions.assertTrue(elapsed2.toMillis() <= 1);
        Assertions.assertTrue(elapsed3.toMillis() <= 1);
        AssertUtils.assertRangeInclusive(450, 550, elapsed4);
        AssertUtils.assertRangeInclusive(2000, 2200, elapsedOverall);

        Assertions.assertEquals(1, value1);
        Assertions.assertEquals(2, value2);
        Assertions.assertEquals(3, value3);
        Assertions.assertEquals(3, value4);
        Assertions.assertEquals(2, value5);
        Assertions.assertEquals(1, value6);
        Assertions.assertEquals(0, value7);
    }

    @Test
    void threadSafetyTest() throws InterruptedException {
        final ThrottledCounter counter = new ThrottledCounter(5, 100);
        AtomicReference<Exception> error = new AtomicReference<>();

        final Runnable target = () -> {
            try {
                for (int i = 0; i < 20; i++) {
                    incrementAndScheduleDecrement(counter);
                }
            } catch (Exception exception) {
                error.set(exception);
            }
        };

        final List<Thread> threads = Stream.generate(() -> new Thread(target)).limit(5).toList();

        threads.forEach(Thread::start);

        for (final Thread thread : threads) {
            thread.join(3000);
            Assertions.assertEquals(Thread.State.TERMINATED, thread.getState(), "probably deadlock");
        }

        Assertions.assertNull(error.get());
    }

    private void incrementAndScheduleDecrement(final ThrottledCounter counter) {
        counter.increment();
        counter.scheduleDecrement();
    }

}