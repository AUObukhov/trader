package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import ru.obukhov.trader.common.model.ExecutionResult;

import java.time.Duration;
import java.util.function.Supplier;

@UtilityClass
public class ExecutionUtils {

    public static Duration run(final Runnable runnable) {
        final long start = System.nanoTime();

        runnable.run();

        final long end = System.nanoTime();
        return Duration.ofNanos(end - start);
    }

    /**
     * Executes given {@code supplier}. Does not catches execution exceptions.
     *
     * @return result of execution of given {@code supplier} and execution time.
     */
    public static <T> ExecutionResult<T> get(final Supplier<T> supplier) {
        final long start = System.nanoTime();

        final T result = supplier.get();

        final long end = System.nanoTime();
        final Duration duration = Duration.ofNanos(end - start);

        return new ExecutionResult<>(result, duration, null);
    }

    /**
     * Safely executes given {@code supplier}
     *
     * @return result of execution of given {@code supplier} and execution time and exception if it has occurred during execution
     */
    public static <T> ExecutionResult<T> getSafe(final ThrowingSupplier<T> supplier) {
        T result = null;
        Duration duration;
        Exception exception = null;

        final long start = System.nanoTime();
        try {
            result = supplier.get();
        } catch (final Exception caughtException) {
            exception = caughtException;
        } finally {
            final long end = System.nanoTime();
            duration = Duration.ofNanos(end - start);
        }

        return new ExecutionResult<>(result, duration, exception);
    }

}
