package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.model.ExecutionResult;

class ExecutionUtilsTest {

    @Test
    void get_returnsSupplierResult_andNonNegativeDuration_andNullException_whenSupplierSucceeds() {
        final String supplierResult = "result";

        final ExecutionResult<String> executionResult = ExecutionUtils.get(() -> supplierResult);

        Assertions.assertEquals(supplierResult, executionResult.getResult());
        Assertions.assertFalse(executionResult.getDuration().isNegative());
        Assertions.assertNull(executionResult.getException());
    }

    @Test
    void get_returnsNullSupplierResult_andNonNegativeDuration_andException_whenSupplierFails() {
        final String exceptionMessage = "exception message";

        final ExecutionResult<String> executionResult = ExecutionUtils.get(() -> {
            throw new IllegalArgumentException(exceptionMessage);
        });

        Assertions.assertNull(executionResult.getResult());
        Assertions.assertFalse(executionResult.getDuration().isNegative());
        Assertions.assertNotNull(executionResult.getException());
        Assertions.assertEquals(IllegalArgumentException.class, executionResult.getException().getClass());
        Assertions.assertEquals(exceptionMessage, executionResult.getException().getMessage());
    }

}