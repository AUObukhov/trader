package ru.obukhov.trader.common.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectionsUtils {

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

    /**
     * @return list with last {@code size} elements of given {@code list}
     */
    public static <T> List<T> getTail(List<T> list, int size) {
        if (size >= list.size()) {
            return list;
        }

        return list.subList(list.size() - size, list.size());
    }

    /**
     * Interpolates element at given {@code index} in given {@code list} and inserts it there.<br/>
     * If {@code index} = 0, then interpolated element is equal to first element of {@code list}.<br/>
     * If {@code index} = {@code list.size()} then interpolated element is equal to last element of {@code list}.<br/>
     * Otherwise interpolated element is computed by given {@code interpolator} from
     * {@code list.get(index - 1)} and {@code list.get(index)}
     *
     * @throws IllegalArgumentException {@code index} is negative or greater then size of list
     * @throws IllegalArgumentException {@code list} is empty
     */
    public static <T> void insertInterpolated(List<T> list, int index, BinaryOperator<T> interpolator) {
        Assert.isTrue(index >= 0, "index can't be negative");
        Assert.isTrue(index <= list.size(), "index can't be greater than size of list");
        Assert.isTrue(!list.isEmpty(), "list can't be empty");

        if (index == 0) {
            list.add(0, list.get(0));
        } else if (index == list.size()) {
            list.add(Iterables.getLast(list));
        } else {
            T value = interpolator.apply(list.get(index - 1), list.get(index));
            list.add(index, value);
        }
    }

    /**
     * @return true, when given {@code list} contains all elements of given {@code seachedList} in same order, otherwise false.
     * Elements compared by {@link Objects#equals(Object, Object)}
     * @throws IllegalArgumentException when any of given lists is null
     */
    public static <T> boolean containsList(List<T> list, List<T> searchedList) {
        Assert.notNull(list, "list must not be null");
        Assert.notNull(searchedList, "searchedList must not be null");

        if (searchedList.isEmpty()) {
            return true;
        }
        if (list.isEmpty()) {
            return false;
        }

        for (int i = 0; i < list.size() - searchedList.size() + 1; i++) {
            if (containsList(list, searchedList, i)) {
                return true;
            }
        }

        return false;
    }

    private static <T> boolean containsList(List<T> list, List<T> searchedList, int searchStartPosition) {
        for (int j = 0; j < searchedList.size(); j++) {
            if (!Objects.equals(list.get(searchStartPosition + j), searchedList.get(j))) {
                return false;
            }
        }

        return true;
    }

}