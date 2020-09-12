package ru.obukhov.investor.util;

import com.google.common.base.Stopwatch;
import org.junit.Assert;
import org.junit.Test;

public class ThrottledCounterTest {

    @Test
    public void test() throws InterruptedException {

        ThrottledCounter counter = new ThrottledCounter(1000, 3);
        counter.increment();
        int value1 = counter.getValue();

        Thread.sleep(500);
        int value2 = counter.getValue();

        counter.increment();
        counter.increment();
        int value3 = counter.getValue();

        Thread.sleep(750);
        int value4 = counter.getValue();

        Thread.sleep(500);
        int value5 = counter.getValue();

        Assert.assertEquals(1, value1);
        Assert.assertEquals(1, value2);
        Assert.assertEquals(3, value3);
        Assert.assertEquals(2, value4);
        Assert.assertEquals(0, value5);
    }

    @Test
    public void test_waiting_whenValueIsMax() throws InterruptedException {

        ThrottledCounter counter = new ThrottledCounter(1000, 3);
        Stopwatch stopwatch = Stopwatch.createStarted();

        counter.increment();
        int value1 = counter.getValue();

        Thread.sleep(250);
        counter.increment();
        int value2 = counter.getValue();

        Thread.sleep(250);
        counter.increment();
        int value3 = counter.getValue();

        counter.increment(); // waits for 500 milliseconds
        int value4 = counter.getValue();

        Thread.sleep(500);
        int value5 = counter.getValue();

        Thread.sleep(250);
        int value6 = counter.getValue();

        Thread.sleep(500);
        int value7 = counter.getValue();

        stopwatch.stop();

        Assert.assertTrue(stopwatch.elapsed().toMillis() >= 2250);
        Assert.assertEquals(1, value1);
        Assert.assertEquals(2, value2);
        Assert.assertEquals(3, value3);
        Assert.assertEquals(3, value4);
        Assert.assertEquals(2, value5);
        Assert.assertEquals(1, value6);
        Assert.assertEquals(0, value7);
    }
}