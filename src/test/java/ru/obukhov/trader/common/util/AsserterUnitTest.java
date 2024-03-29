package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.exception.TestException;

import java.util.function.Supplier;

class AsserterUnitTest {

    @Test
    @SuppressWarnings("ConstantConditions")
    void notNull_throwsException_whenObjectIsNull() {
        final String exceptionMessage = "test exception message";
        final Supplier<RuntimeException> exceptionSupplier = () -> new TestException(exceptionMessage);
        final Executable executable = () -> Asserter.notNull(null, exceptionSupplier);
        AssertUtils.assertThrowsWithMessage(TestException.class, executable, exceptionMessage);
    }

    @Test
    void notNull_doesNotThrowException_whenObjectIsNotNull() {
        final String exceptionMessage = "test exception message";
        final Supplier<RuntimeException> exceptionSupplier = () -> new TestException(exceptionMessage);
        final Object object = "test object";
        Assertions.assertDoesNotThrow(() -> Asserter.notNull(object, exceptionSupplier));
    }

    @Test
    void isTrue_throwsException_whenFalse() {
        final String exceptionMessage = "test exception message";
        final Supplier<RuntimeException> exceptionSupplier = () -> new TestException(exceptionMessage);
        final Executable executable = () -> Asserter.isTrue(false, exceptionSupplier);
        AssertUtils.assertThrowsWithMessage(TestException.class, executable, exceptionMessage);
    }

    @Test
    void isTrue_doesNotThrowException_whenTrue() {
        final String exceptionMessage = "test exception message";
        final Supplier<RuntimeException> exceptionSupplier = () -> new TestException(exceptionMessage);
        final Executable executable = () -> Asserter.isTrue(true, exceptionSupplier);
        Assertions.assertDoesNotThrow(executable);
    }

}