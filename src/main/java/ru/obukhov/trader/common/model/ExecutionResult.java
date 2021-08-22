package ru.obukhov.trader.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * Data structure with result of some execution and metadata about this execution
 *
 * @param <T> type of execution result
 */
@Data
@AllArgsConstructor
public class ExecutionResult<T> {

    /**
     * Actual result of execution. Must be null if execution failed
     */
    @Nullable
    private final T result;

    /**
     * Duration of execution
     */
    @NotNull
    private final Duration duration;

    /**
     * Exception occurred during execution
     */
    @Nullable
    private final Exception exception;

}