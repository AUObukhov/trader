package ru.obukhov.trader.common.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.util.Map;

class MapUtilsTest {

    // region getRequiredString tests

    @Test
    void getRequiredString_throwsIllegalArgumentException_whenNoValue() {
        final Map<String, Object> map = Map.of("key1", "value1", "key2", "value2");

        AssertUtils.assertThrowsWithMessage(
                () -> MapUtils.getRequiredString(map, "key"),
                IllegalArgumentException.class,
                "\"key\" is mandatory"
        );
    }

    @Test
    void getRequiredString_returnsValue_whenValueExists() {
        final Map<String, Object> map = Map.of("key1", "value1", "key2", "value2");

        final String value = MapUtils.getRequiredString(map, "key1");

        Assertions.assertEquals("value1", value);
    }

    // endregion

    // region getNotBlankString tests

    @Test
    void getNotBlankString_throwsIllegalArgumentException_whenNoValue() {
        final Map<String, Object> map = Map.of("key1", "value1", "key2", "value2");

        AssertUtils.assertThrowsWithMessage(
                () -> MapUtils.getNotBlankString(map, "key"),
                IllegalArgumentException.class,
                "\"key\" must be not blank"
        );
    }

    @Test
    void getNotBlankString_throwsIllegalArgumentException_whenValueIsEmpty() {
        final Map<String, Object> map = Map.of("key1", StringUtils.EMPTY, "key2", "value2");

        AssertUtils.assertThrowsWithMessage(
                () -> MapUtils.getNotBlankString(map, "key1"),
                IllegalArgumentException.class,
                "\"key1\" must be not blank"
        );
    }

    @Test
    void getNotBlankString_throwsIllegalArgumentException_whenValueIsBlank() {
        final Map<String, Object> map = Map.of("key1", "    ", "key2", "value2");

        AssertUtils.assertThrowsWithMessage(
                () -> MapUtils.getNotBlankString(map, "key1"),
                IllegalArgumentException.class,
                "\"key1\" must be not blank"
        );
    }

    @Test
    void getNotBlankString_returnsValue_whenValueExists() {
        final Map<String, Object> map = Map.of("key1", "value1", "key2", "value2");

        final String value = MapUtils.getNotBlankString(map, "key1");

        Assertions.assertEquals("value1", value);
    }

    // endregion

    // region getRequiredInteger tests

    @Test
    void getRequiredInteger_throwsIllegalArgumentException_whenNoValue() {
        final Map<String, Object> map = Map.of("key1", 1, "key2", 2);

        AssertUtils.assertThrowsWithMessage(
                () -> MapUtils.getRequiredInteger(map, "key"),
                IllegalArgumentException.class,
                "\"key\" is mandatory"
        );
    }

    @Test
    void getRequiredInteger_returnsValue_whenValueExists() {
        final Map<String, Object> map = Map.of("key1", 1, "key2", 2);

        final Integer value = MapUtils.getRequiredInteger(map, "key1");

        Assertions.assertEquals(1, value);
    }

    // endregion

}