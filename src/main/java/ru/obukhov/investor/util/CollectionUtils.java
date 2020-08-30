package ru.obukhov.investor.util;

import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectionUtils {

    /**
     * Collapses {@code multimap} values collection to single value by {@code valueMapper}
     *
     * @param multimap    multimap to collapse
     * @param valueMapper function, collapsing collection to single value
     * @param <K>         key type
     * @param <V>         value type
     * @return {@link Map} with same keys as {@code multimap} and collapsed values
     */
    public static <K, V> Map<K, V> reduceMultimap(Multimap<K, V> multimap, Function<Collection<V>, V> valueMapper) {

        Map<K, V> result = new HashMap<>();
        for (K key : multimap.keySet()) {
            final V value = valueMapper.apply(multimap.get(key));
            result.put(key, value);
        }

        return result;

    }

}