package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;

class ValidationUtilsUnitTest {

    // region assertNullConsistent tests

    @Test
    @SuppressWarnings("all")
    void assertNullConsistent_throwsIllegalArgumentException_whenObject1IsNullAndObjectIsNotNull() {
        final String message = "test message";

        AssertUtils.assertThrowsWithMessage(
                () -> ValidationUtils.assertNullConsistent(null, "object2", message),
                IllegalArgumentException.class,
                message
        );
    }

    @Test
    @SuppressWarnings("all")
    void assertNullConsistent_throwsIllegalArgumentException_whenObject1IsNotNullAndObjectIsNull() {
        final String message = "test message";

        AssertUtils.assertThrowsWithMessage(
                () -> ValidationUtils.assertNullConsistent("object1", null, message),
                IllegalArgumentException.class,
                message
        );
    }

    @Test
    void assertNullConsistent_doesNotThrowsException_whenBothObjectAreNotNull() {
        final String message = "test message";

        ValidationUtils.assertNullConsistent("object1", "object2", message);
    }

    // endregion

}