package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@UtilityClass
public class MapUtils {

    public static String getRequiredString(final Map<String, Object> map, final String key) {
        final String value = (String) map.get(key);
        Assert.notNull(value, () -> "\"" + key + "\" is mandatory");
        return value;
    }

    public static String getNotBlankString(final Map<String, Object> map, final String key) {
        final String value = (String) map.get(key);
        Assert.isTrue(StringUtils.isNotBlank(value), () -> "\"" + key + "\" must be not blank");
        return value;
    }

    public static Integer getRequiredInteger(final Map<String, Object> map, final String key) {
        final Integer value = (Integer) map.get(key);
        Assert.notNull(value, () -> "\"" + key + "\" is mandatory");
        return value;
    }

    public static <T, K> Collector<T, ?, Map<K, T>> newMapKeyCollector(
            final Function<? super T, ? extends K> keyMapper
    ) {
        return Collectors.toMap(keyMapper, Function.identity());
    }

    public static <T, V> Collector<T, ?, Map<T, V>> newMapValueCollector(
            final Function<? super T, ? extends V> valueMapper
    ) {
        return Collectors.toMap(Function.identity(), valueMapper);
    }

    public static <T, V> Collector<T, ?, SequencedMap<T, V>> newSequencedMapValueCollector(
            final Function<? super T, ? extends V> valueMapper
    ) {
        BinaryOperator<V> mergeFunction = (x1, x2) -> {
            throw new IllegalStateException("Unexpected merge");
        };
        return Collectors.toMap(Function.identity(), valueMapper, mergeFunction, LinkedHashMap::new);
    }

    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> newMapEntryCollector() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public static <K, V extends Comparable<V>> SequencedMap<K, V> sortByValue(final Map<K, V> map) {
        final LinkedHashMap<K, V> result = new LinkedHashMap<>();
        map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return result;
    }

}