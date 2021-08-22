package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class ValidationUtilsUnitTest {

    // region assertNullConsistent tests

    @Test
    @SuppressWarnings("all")
    void assertNullConsistent_throwsIllegalArgumentException_whenObject1IsNullAndObjectIsNotNull() {
        final String message = "test message";

        final Executable executable = () -> ValidationUtils.assertNullConsistent(null, "object2", message);
        Assertions.assertThrows(IllegalArgumentException.class, executable, message);
    }

    @Test
    @SuppressWarnings("all")
    void assertNullConsistent_throwsIllegalArgumentException_whenObject1IsNotNullAndObjectIsNull() {
        final String message = "test message";

        final Executable executable = () -> ValidationUtils.assertNullConsistent("object1", null, message);
        Assertions.assertThrows(IllegalArgumentException.class, executable, message);
    }

    @Test
    void assertNullConsistent_doesNotThrowsException_whenBothObjectAreNotNull() {
        final String message = "test message";

        ValidationUtils.assertNullConsistent("object1", "object2", message);
    }

    // endregion

}