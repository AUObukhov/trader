package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import ru.obukhov.trader.common.model.ExecutionResult;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.function.Supplier;

@UtilityClass
public class ExecutionUtils {

    /**
     * Executes given {@code supplier}. Does not catches execution exceptions.
     *
     * @return result of execution of given {@code supplier} and execution time.
     */
    public static <T> ExecutionResult<T> get(Supplier<T> supplier) {
        final OffsetDateTime start = OffsetDateTime.now();

        final T result = supplier.get();

        final OffsetDateTime end = OffsetDateTime.now();
        final Duration duration = Duration.between(start, end);

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

        final OffsetDateTime start = OffsetDateTime.now();
        try {
            result = supplier.get();
        } catch (Exception caughtException) {
            exception = caughtException;
        } finally {
            final OffsetDateTime end = OffsetDateTime.now();
            duration = Duration.between(start, end);
        }

        return new ExecutionResult<>(result, duration, exception);
    }

}
