package ru.obukhov.investor.util;

import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CollectionUtils {

    public static <K, V> Map<K, V> reduceMultimap(Multimap<K, V> multimap, Function<Collection<V>, V> valueMapper) {

        Map<K, V> result = new HashMap<>();
        for (K key : multimap.keySet()) {
            final V value = valueMapper.apply(multimap.get(key));
            result.put(key, value);
        }

        return result;

    }

}