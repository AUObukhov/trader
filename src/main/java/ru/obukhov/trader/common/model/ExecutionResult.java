package ru.obukhov.trader.common.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public record ExecutionResult<T>(
        @Nullable T result, // Actual result of execution. Must be null if execution failed
        @NotNull Duration duration, // Duration of execution
        @Nullable Exception exception // Exception occurred during execution
) {
}