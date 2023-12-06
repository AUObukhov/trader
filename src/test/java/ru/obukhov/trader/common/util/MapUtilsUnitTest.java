package ru.obukhov.trader.common.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.util.List;
import java.util.Map;

class MapUtilsUnitTest {

    // region getRequiredString tests

    @Test
    void getRequiredString_throwsIllegalArgumentException_whenNoValue() {
        final Map<String, Object> map = Map.of("key1", "value1", "key2", "value2");

        final Executable executable = () -> MapUtils.getRequiredString(map, "key");
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "\"key\" is mandatory");
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

        final Executable executable = () -> MapUtils.getNotBlankString(map, "key");
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "\"key\" must be not blank");
    }

    @Test
    void getNotBlankString_throwsIllegalArgumentException_whenValueIsEmpty() {
        final Map<String, Object> map = Map.of("key1", StringUtils.EMPTY, "key2", "value2");

        final Executable executable = () -> MapUtils.getNotBlankString(map, "key1");
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "\"key1\" must be not blank");
    }

    @Test
    void getNotBlankString_throwsIllegalArgumentException_whenValueIsBlank() {
        final Map<String, Object> map = Map.of("key1", "    ", "key2", "value2");

        final Executable executable = () -> MapUtils.getNotBlankString(map, "key1");
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "\"key1\" must be not blank");
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

        final Executable executable = () -> MapUtils.getRequiredInteger(map, "key");
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "\"key\" is mandatory");
    }

    @Test
    void getRequiredInteger_returnsValue_whenValueExists() {
        final Map<String, Object> map = Map.of("key1", 1, "key2", 2);

        final Integer value = MapUtils.getRequiredInteger(map, "key1");

        Assertions.assertEquals(1, value);
    }

    // endregion

    @Test
    void newMapKeyCollector() {
        final List<String> values = List.of("val1", "value2", "value 3");

        final Map<Integer, String> actualResult = values.stream().collect(MapUtils.newMapKeyCollector(String::length));

        final Map<Integer, String> expectedResult = Map.of(
                4, "val1",
                6, "value2",
                7, "value 3"
        );

        AssertUtils.assertEquals(expectedResult, actualResult);
    }

    @Test
    void newMapValueCollector() {
        final List<String> values = List.of("k1", "key2", "key 3");

        final Map<String, Integer> actualResult = values.stream().collect(MapUtils.newMapValueCollector(String::length));

        final Map<String, Integer> expectedResult = Map.of(
                "k1", 2,
                "key2", 4,
                "key 3", 5
        );

        AssertUtils.assertEquals(expectedResult, actualResult);
    }

    @Test
    void newMapEntryCollector() {
        final Map<String, String> map = Map.of(
                "key1", "value1",
                "key2", "value2",
                "key3", "value3"
        );

        final Map<String, String> result = map.entrySet().stream().collect(MapUtils.newMapEntryCollector());

        AssertUtils.assertEquals(map, result);
    }

}