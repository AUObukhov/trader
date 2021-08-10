package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationUtils {

    /**
     * @throws IllegalArgumentException with given {@code message} if one and only one of {@code object1} and {@code object2} is null
     */
    public static void assertNullConsistent(final Object object1, final Object object2, final String message) {
        if (object1 == null && object2 != null || object1 != null && object2 == null) {
            throw new IllegalArgumentException(message);
        }
    }

}