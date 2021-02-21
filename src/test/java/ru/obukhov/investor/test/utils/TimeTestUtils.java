package ru.obukhov.investor.test.utils;

import com.google.common.base.Stopwatch;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TimeTestUtils {

    public static void executeAndAssertFaster(CheckedRunnable runnable, long time) throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        runnable.run();
        stopwatch.stop();
        long elapsed = stopwatch.elapsed().toMillis();
        AssertUtils.assertFaster(time, elapsed);
    }

    public static void executeAndAssertSlower(CheckedRunnable runnable, long time) throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        runnable.run();
        stopwatch.stop();
        long elapsed = stopwatch.elapsed().toMillis();
        AssertUtils.assertSlower(time, elapsed);
    }

}
