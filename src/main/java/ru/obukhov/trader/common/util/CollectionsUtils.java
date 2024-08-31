package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

@UtilityClass
public class CollectionsUtils {

    @NotNull
    public static <T> List<T> getTail(final List<T> list, final int size) {
        if (size >= list.size()) {
            return list;
        }

        return list.subList(list.size() - size, list.size());
    }

    public static <T> T getLast(final Iterable<T> iterable) {
        if (iterable == null) {
            return null;
        }

        final Iterator<T> iterator = iterable.iterator();
        T last = null;
        while (iterator.hasNext()) {
            last = iterator.next();
        }

        return last;
    }

    public static <T> void insertInterpolated(final List<T> list, final int index, final BinaryOperator<T> interpolator) {
        Assert.isTrue(index >= 0, "index can't be negative");
        Assert.isTrue(index <= list.size(), "index can't be greater than size of list");
        Assert.isTrue(!list.isEmpty(), "list can't be empty");

        if (index == 0) {
            list.add(0, list.getFirst());
        } else if (index == list.size()) {
            list.add(getLast(list));
        } else {
            T value = interpolator.apply(list.get(index - 1), list.get(index));
            list.add(index, value);
        }
    }

    public static <T> int binarySearch(final List<? extends T> list, final T key, final Comparator<? super T> comparator) {
        int searchResult = Collections.binarySearch(list, key, comparator);
        return searchResult < 0 ? -1 - searchResult : searchResult;
    }

    public static <T, U extends Comparable<U>> List<T> filterOrderedList(
            final List<T> list,
            final T keyItem,
            final Function<? super T, ? extends U> keyExtractor
    ) {
        final Comparator<? super T> comparator = Comparator.comparing(keyExtractor);
        final int index = CollectionsUtils.binarySearch(list, keyItem, comparator);
        return list.subList(0, index);
    }

}