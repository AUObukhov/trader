package ru.obukhov.trader.common.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class CollectionsUtilsUnitTest {

    @Test
    void reduceMultimap_average() {
        Multimap<String, BigDecimal> multimap = MultimapBuilder.hashKeys().arrayListValues().build();

        multimap.put("key1", BigDecimal.valueOf(10));

        multimap.put("key2", BigDecimal.valueOf(1));
        multimap.put("key2", BigDecimal.valueOf(2));
        multimap.put("key2", BigDecimal.valueOf(3));
        multimap.put("key2", BigDecimal.valueOf(4));

        Map<String, BigDecimal> result = CollectionsUtils.reduceMultimap(multimap, MathUtils::getAverage);

        AssertUtils.assertEquals(10, result.get("key1"));
        AssertUtils.assertEquals(2.5, result.get("key2"));
    }

    // region getTail tests

    @Test
    void getTail_returnsEmptyList_whenSizeIsZero() {
        List<Integer> list = Arrays.asList(0, 1, 2, 3, 4);

        List<Integer> result = CollectionsUtils.getTail(list, 0);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getTail_returnsTail_whenSizeIsLowerThanListSize() {
        List<Integer> list = Arrays.asList(0, 1, 2, 3, 4);
        int size = 3;
        List<Integer> result = CollectionsUtils.getTail(list, size);

        Assertions.assertEquals(size, result.size());
        Assertions.assertEquals(Integer.valueOf(2), result.get(0));
        Assertions.assertEquals(Integer.valueOf(3), result.get(1));
        Assertions.assertEquals(Integer.valueOf(4), result.get(2));
    }

    @Test
    void getTail_returnsEqualList_whenSizeIsEqualToListSize() {
        List<Integer> list = Arrays.asList(0, 1, 2, 3, 4);
        int size = 5;
        List<Integer> result = CollectionsUtils.getTail(list, size);

        Assertions.assertEquals(list.size(), result.size());
        for (int i = 0; i < list.size(); i++) {
            Assertions.assertEquals(list.get(0), result.get(0));
        }
    }

    @Test
    void getTail_returnsEqualList_whenSizeIsGreaterThanListSize() {
        List<Integer> list = Arrays.asList(0, 1, 2, 3, 4);
        int size = 6;
        List<Integer> result = CollectionsUtils.getTail(list, size);

        Assertions.assertEquals(list.size(), result.size());
        for (int i = 0; i < list.size(); i++) {
            Assertions.assertEquals(list.get(0), result.get(0));
        }
    }

    // endregion

    // region getLast tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetLast() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(Collections.emptyList(), null),
                Arguments.of(ImmutableList.of(1, 2, 3), 3)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLast")
    <T> void getLast_returnsLastItem_whenIterablesIsNotEmpty(Iterable<T> iterable, T expectedLastItem) {
        T lastItem = CollectionsUtils.getLast(iterable);

        Assertions.assertEquals(expectedLastItem, lastItem);
    }

    // endregion

    // region insertInterpolated tests

    @Test
    void insertInterpolated_throwsIllegalArgumentException_whenIndexIsNegative() {
        List<BigDecimal> list = Arrays.asList(BigDecimal.ONE, BigDecimal.TEN);
        int index = -1;

        AssertUtils.assertThrowsWithMessage(
                () -> CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage),
                IllegalArgumentException.class,
                "index can't be negative"
        );
    }

    @Test
    void insertInterpolated_throwsIllegalArgumentException_whenIndexIsGreaterThanListSize() {
        List<BigDecimal> list = Arrays.asList(BigDecimal.ONE, BigDecimal.TEN);
        int index = 3;

        AssertUtils.assertThrowsWithMessage(
                () -> CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage),
                IllegalArgumentException.class,
                "index can't be greater than size of list"
        );
    }

    @Test
    void insertInterpolated_throwsIllegalArgumentException_whenListIsEmpty() {
        List<BigDecimal> list = new ArrayList<>();
        int index = 0;

        AssertUtils.assertThrowsWithMessage(
                () -> CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage),
                IllegalArgumentException.class,
                "list can't be empty"
        );
    }

    @Test
    void insertInterpolated_addsElementToBeginning_whenIndexIsZero() {
        List<BigDecimal> list = Lists.newArrayList(BigDecimal.valueOf(5), BigDecimal.TEN);
        int index = 0;

        CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage);

        Assertions.assertEquals(3, list.size());
        AssertUtils.assertEquals(5, list.get(0));
        AssertUtils.assertEquals(5, list.get(1));
        AssertUtils.assertEquals(10, list.get(2));
    }

    @Test
    void insertInterpolated_addsElementToEnd_whenIndexIsEqualsToListSize() {
        List<BigDecimal> list = Lists.newArrayList(BigDecimal.valueOf(5), BigDecimal.TEN);
        int index = 2;

        CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage);

        Assertions.assertEquals(3, list.size());
        AssertUtils.assertEquals(5, list.get(0));
        AssertUtils.assertEquals(10, list.get(1));
        AssertUtils.assertEquals(10, list.get(2));
    }

    @Test
    void insertInterpolated_addsElementToMiddle_whenIndexIsInMiddle() {
        List<BigDecimal> list = Lists.newArrayList(BigDecimal.valueOf(5), BigDecimal.TEN, BigDecimal.ZERO);
        int index = 1;

        CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage);

        Assertions.assertEquals(4, list.size());
        AssertUtils.assertEquals(5, list.get(0));
        AssertUtils.assertEquals(7.5, list.get(1));
        AssertUtils.assertEquals(10, list.get(2));
        AssertUtils.assertEquals(0, list.get(3));
    }

    // endregion

    // region containsList tests

    @Test
    void containsList_throwsIllegalArgumentException_whenListIsNull() {
        List<String> list = null;
        List<String> searchedList = Arrays.asList("0", "1");

        AssertUtils.assertThrowsWithMessage(
                () -> CollectionsUtils.containsList(list, searchedList),
                IllegalArgumentException.class,
                "list must not be null"
        );
    }

    @Test
    void containsList_throwsIllegalArgumentException_whenSearchedListIsNull() {
        List<String> list = Arrays.asList("0", "1");
        List<String> searchedList = null;

        AssertUtils.assertThrowsWithMessage(
                () -> CollectionsUtils.containsList(list, searchedList),
                IllegalArgumentException.class,
                "searchedList must not be null"
        );
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forContainsList() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), Collections.emptyList(), true),
                Arguments.of(Arrays.asList("0", "1"), Collections.emptyList(), true),
                Arguments.of(Arrays.asList("0", "1"), Arrays.asList("0", "1"), true),
                Arguments.of(Arrays.asList("0", "1", "2", "3"), Arrays.asList("0", "1"), true),
                Arguments.of(Arrays.asList("0", "1", "2", "3"), Arrays.asList("1", "2"), true),
                Arguments.of(Arrays.asList("0", "1", "2", "3"), Arrays.asList("2", "3"), true),
                Arguments.of(Collections.emptyList(), Collections.singletonList("0"), false),
                Arguments.of(Arrays.asList("0", "1", "2", "3"), Arrays.asList("5", "6"), false),
                Arguments.of(Arrays.asList("0", "1", "2", "3"), Arrays.asList("2", "3", "4"), false),
                Arguments.of(Arrays.asList("0", "1", "2", "3"), Arrays.asList("2", "1"), false)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forContainsList")
    void containsList(List<String> list, List<String> searchedList, boolean expectedResult) {
        boolean result = CollectionsUtils.containsList(list, searchedList);

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

}