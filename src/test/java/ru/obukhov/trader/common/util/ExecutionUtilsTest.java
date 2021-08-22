package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.model.ExecutionResult;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

class ExecutionUtilsTest {

    // region run tests

    @Test
    void run_executesRunnable_andReturnsNonNegativeDuration() {
        final AtomicInteger integer = new AtomicInteger(0);

        final Duration duration = ExecutionUtils.run(integer::incrementAndGet);

        Assertions.assertEquals(1, integer.get());
        Assertions.assertFalse(duration.isNegative());
    }

    @Test
    void run_throwsRunnableException() {
        final IllegalArgumentException runnableException = new IllegalArgumentException("exception message");
        final Runnable runnable = () -> {
            throw runnableException;
        };

        AssertUtils.assertThrowsWithMessage(() -> ExecutionUtils.run(runnable), runnableException.getClass(), runnableException.getMessage());
    }

    // endregion

    // region get tests

    @Test
    void get_returnsSupplierResult_andNonNegativeDuration_andNullException_whenSupplierSucceeds() {
        final String supplierResult = "result";

        final ExecutionResult<String> executionResult = ExecutionUtils.get(() -> supplierResult);

        Assertions.assertEquals(supplierResult, executionResult.getResult());
        Assertions.assertFalse(executionResult.getDuration().isNegative());
        Assertions.assertNull(executionResult.getException());
    }

    @Test
    void get_throwsSupplierException() {
        final IllegalArgumentException supplierException = new IllegalArgumentException("exception message");
        final Supplier<String> supplier = () -> {
            throw supplierException;
        };

        AssertUtils.assertThrowsWithMessage(() -> ExecutionUtils.get(supplier), supplierException.getClass(), supplierException.getMessage());
    }

    // endregion

    // region getSafe tests

    @Test
    void getSafe_returnsSupplierResult_andNonNegativeDuration_andNullException_whenSupplierSucceeds() {
        final String supplierResult = "result";

        final ExecutionResult<String> executionResult = ExecutionUtils.getSafe(() -> supplierResult);

        Assertions.assertEquals(supplierResult, executionResult.getResult());
        Assertions.assertFalse(executionResult.getDuration().isNegative());
        Assertions.assertNull(executionResult.getException());
    }

    @Test
    void getSafe_returnsNullSupplierResult_andNonNegativeDuration_andException_whenSupplierFails() {
        final IllegalArgumentException supplierException = new IllegalArgumentException("exception message");

        final ExecutionResult<String> executionResult = ExecutionUtils.getSafe(() -> {
            throw supplierException;
        });

        Assertions.assertNull(executionResult.getResult());
        Assertions.assertFalse(executionResult.getDuration().isNegative());
        Assertions.assertNotNull(executionResult.getException());
        Assertions.assertEquals(supplierException.getClass(), executionResult.getException().getClass());
        Assertions.assertEquals(supplierException.getMessage(), executionResult.getException().getMessage());
    }

    // endregion

}