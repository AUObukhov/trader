package ru.obukhov.trader.common.util;

import com.google.common.base.Stopwatch;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

public class ThrottledCounterTest {

    @Test
    public void decrementingValue_whenIntervalPassed() throws InterruptedException {

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

        Assert.assertEquals(1, value1);
        Assert.assertEquals(1, value2);
        Assert.assertEquals(3, value3);
        Assert.assertEquals(2, value4);
        Assert.assertEquals(0, value5);
    }

    @Test
    public void waiting_whenValueIsMax() throws InterruptedException {

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

        MatcherAssert.assertThat(stopwatch.elapsed().toMillis(), allOf(greaterThan(2000L), lessThan(2200L)));

        Assert.assertEquals(1, value1);
        Assert.assertEquals(0, duration1.toMillis());

        Assert.assertEquals(2, value2);
        Assert.assertEquals(0, duration2.toMillis());

        Assert.assertEquals(3, value3);
        Assert.assertEquals(0, duration3.toMillis());

        Assert.assertEquals(3, value4);
        MatcherAssert.assertThat(duration4.toMillis(), allOf(greaterThan(450L), lessThan(550L)));
        Assert.assertEquals(2, value5);
        Assert.assertEquals(1, value6);
        Assert.assertEquals(0, value7);
    }

}