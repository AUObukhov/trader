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

    /**
     * @param <T> stream items type
     * @param <K> result map keys type
     * @return collector, creating map with keys provided by given {@code keyMapper} and stream items as values
     */
    public static <T, K> Collector<T, ?, Map<K, T>> newMapKeyCollector(final Function<? super T, ? extends K> keyMapper) {
        return Collectors.toMap(keyMapper, Function.identity());
    }

    /**
     * @param <T> stream items type
     * @param <V> result map value type
     * @return collector, creating map with keys provided by given {@code valueMapper} and stream items as values
     */
    public static <T, V> Collector<T, ?, Map<T, V>> newMapValueCollector(final Function<? super T, ? extends V> valueMapper) {
        return Collectors.toMap(Function.identity(), valueMapper);
    }

    /**
     * @param <T> stream items type
     * @param <V> result map value type
     * @return collector, creating SequencedMap with keys provided by given {@code valueMapper} and stream items as values
     */
    public static <T, V> Collector<T, ?, SequencedMap<T, V>> newSequencedMapValueCollector(final Function<? super T, ? extends V> valueMapper) {
        BinaryOperator<V> mergeFunction = (x1, x2) -> {
            throw new IllegalStateException("Unexpected merge");
        };
        return Collectors.toMap(Function.identity(), valueMapper, mergeFunction, LinkedHashMap::new);
    }

    /**
     * @param <K> result map key type
     * @param <V> result map value type
     * @return collector, creating map from stream items of type {@link Map.Entry}
     */
    public static <K, V> java.util.stream.Collector<Map.Entry<K, V>, ?, java.util.Map<K, V>> newMapEntryCollector() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

}