package ru.obukhov.investor.util;

import com.google.common.collect.Multimap;

import java.time.LocalTime;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class CollectionUtils {

    public static <K extends Comparable<LocalTime>, V> Map<K, V> reduceMultimap(
            Multimap<K, V> multimap, Function<Collection<V>, V> valueMapper) {
        Map<K, V> result = new TreeMap<>();
        for (K key : multimap.keySet()) {
            final V value = valueMapper.apply(multimap.get(key));
            result.put(key, value);
        }
        return result;
    }

}