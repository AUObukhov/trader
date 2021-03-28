package ru.obukhov.trader.common.util;

import com.google.common.base.Stopwatch;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

class ThrottledCounterTest {

    @Test
    void decrementingValue_whenIntervalPassed() throws InterruptedException {

        ThrottledCounter counter = new ThrottledCounter(1000, 3);
        counter.increment();
        int value1 = counter.getValue();

        TimeUnit.MILLISECONDS.sleep(500);
        int value2 = counter.getValue();

        counter.increment();
        counter.increment();
        int value3 = counter.getValue();

        TimeUnit.MILLISECONDS.sleep(750);
        int value4 = counter.getValue();

        TimeUnit.MILLISECONDS.sleep(500);
        int value5 = counter.getValue();

        Assertions.assertEquals(1, value1);
        Assertions.assertEquals(1, value2);
        Assertions.assertEquals(3, value3);
        Assertions.assertEquals(2, value4);
        Assertions.assertEquals(0, value5);
    }

    @Test
    void waiting_whenValueIsMax() throws InterruptedException {

        ThrottledCounter counter = new ThrottledCounter(1000, 3);
        Stopwatch stopwatch = Stopwatch.createStarted();

        Duration duration1 = counter.increment();
        int value1 = counter.getValue();

        TimeUnit.MILLISECONDS.sleep(250); // not changing
        Duration duration2 = counter.increment();
        int value2 = counter.getValue();

        TimeUnit.MILLISECONDS.sleep(250); // not changing
        Duration duration3 = counter.increment();
        int value3 = counter.getValue();

        Duration duration4 = counter.increment(); // waits for ~500 milliseconds until -1, then +1
        int value4 = counter.getValue();

        TimeUnit.MILLISECONDS.sleep(50); // overhead buffer
        TimeUnit.MILLISECONDS.sleep(250); // -1
        int value5 = counter.getValue();

        TimeUnit.MILLISECONDS.sleep(250); // -1
        int value6 = counter.getValue();

        TimeUnit.MILLISECONDS.sleep(500);
        int value7 = counter.getValue();

        stopwatch.stop();

        MatcherAssert.assertThat(
                stopwatch.elapsed().toMillis(),
                Matchers.allOf(Matchers.greaterThan(2000L), Matchers.lessThan(2200L))
        );

        Assertions.assertEquals(1, value1);
        Assertions.assertEquals(0, duration1.toMillis());

        Assertions.assertEquals(2, value2);
        Assertions.assertEquals(0, duration2.toMillis());

        Assertions.assertEquals(3, value3);
        Assertions.assertEquals(0, duration3.toMillis());

        Assertions.assertEquals(3, value4);
        MatcherAssert.assertThat(
                duration4.toMillis(),
                Matchers.allOf(Matchers.greaterThan(450L), Matchers.lessThan(550L))
        );
        Assertions.assertEquals(2, value5);
        Assertions.assertEquals(1, value6);
        Assertions.assertEquals(0, value7);
    }

}