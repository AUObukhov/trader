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

    /**
     * @return list with last {@code size} elements of given {@code list}
     */
    @NotNull
    public static <T> List<T> getTail(final List<T> list, final int size) {
        if (size >= list.size()) {
            return list;
        }

        return list.subList(list.size() - size, list.size());
    }

    /**
     * @return last item of given {@code iterable} or null if {@code iterable} is null or empty
     */
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

    /**
     * Interpolates element at given {@code index} in given {@code list} and inserts it there.<br/>
     * If {@code index} = 0, then interpolated element is equal to first element of {@code list}.<br/>
     * If {@code index} = {@code list.size()} then interpolated element is equal to last element of {@code list}.<br/>
     * Otherwise, interpolated element is computed by given {@code interpolator} from
     * {@code list.get(index - 1)} and {@code list.get(index)}
     *
     * @throws IllegalArgumentException {@code index} is negative or greater than size of list
     * @throws IllegalArgumentException {@code list} is empty
     */
    public static <T> void insertInterpolated(final List<T> list, final int index, final BinaryOperator<T> interpolator) {
        Assert.isTrue(index >= 0, "index can't be negative");
        Assert.isTrue(index <= list.size(), "index can't be greater than size of list");
        Assert.isTrue(!list.isEmpty(), "list can't be empty");

        if (index == 0) {
            list.add(0, list.get(0));
        } else if (index == list.size()) {
            list.add(getLast(list));
        } else {
            T value = interpolator.apply(list.get(index - 1), list.get(index));
            list.add(index, value);
        }
    }

    /**
     * Same as {@link Collections#binarySearch}, but in case if {@code key} not found in list, returns insertion point
     */
    public static <T> int binarySearch(final List<? extends T> list, final T key, final Comparator<? super T> comparator) {
        int searchResult = Collections.binarySearch(list, key, comparator);
        return searchResult < 0 ? -1 - searchResult : searchResult;
    }

    /**
     * Filters naturally ordered list.<br/>
     * Keeps items lower than given {@code keyItem}.<br/>
     * Comparison is done by comparator based on given {@code keyExtractor}.
     *
     * @param list         ordered list
     * @param keyItem      key greater than filtered items. Doesn't have to be present in the {@code list}
     * @param keyExtractor key extractor for comparator. {@code list} must be ordered by that comparator
     * @param <T>          list item type
     * @param <U>          comparable key, retrieved by the comparator
     * @return list of items lower than given {@code keyItem}
     */
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