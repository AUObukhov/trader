package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import ru.obukhov.trader.common.model.ExecutionResult;

import java.time.Duration;
import java.util.function.Supplier;

@UtilityClass
public class ExecutionUtils {

    public static Duration run(final Runnable runnable) {
        final long start = System.currentTimeMillis();

        runnable.run();

        final long end = System.currentTimeMillis();
        return Duration.ofMillis(end - start);
    }

    /**
     * Executes given {@code supplier}. Does not catches execution exceptions.
     *
     * @return result of execution of given {@code supplier} and execution time.
     */
    public static <T> ExecutionResult<T> get(Supplier<T> supplier) {
        final long start = System.currentTimeMillis();

        final T result = supplier.get();

        final long end = System.currentTimeMillis();
        final Duration duration = Duration.ofMillis(end - start);

        return new ExecutionResult<>(result, duration, null);
    }

    /**
     * Safely executes given {@code supplier}
     *
     * @return result of execution of given {@code supplier} and execution time and exception if it has occurred during execution
     */
    public static <T> ExecutionResult<T> getSafe(Supplier<T> supplier) {
        T result = null;
        Duration duration;
        Exception exception = null;

        final long start = System.currentTimeMillis();
        try {
            result = supplier.get();
        } catch (Exception caughtException) {
            exception = caughtException;
        } finally {
            final long end = System.currentTimeMillis();
            duration = Duration.ofMillis(end - start);
        }

        return new ExecutionResult<>(result, duration, exception);
    }

}
