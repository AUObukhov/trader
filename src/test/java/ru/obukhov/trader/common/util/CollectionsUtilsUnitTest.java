package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

class CollectionsUtilsUnitTest {

    // region getTail tests

    @Test
    void getTail_returnsEmptyList_whenSizeIsZero() {
        final List<Integer> list = List.of(0, 1, 2, 3, 4);

        final List<Integer> result = CollectionsUtils.getTail(list, 0);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getTail_returnsTail_whenSizeIsLowerThanListSize() {
        final List<Integer> list = List.of(0, 1, 2, 3, 4);
        final int size = 3;
        final List<Integer> result = CollectionsUtils.getTail(list, size);

        Assertions.assertEquals(size, result.size());
        Assertions.assertEquals(Integer.valueOf(2), result.get(0));
        Assertions.assertEquals(Integer.valueOf(3), result.get(1));
        Assertions.assertEquals(Integer.valueOf(4), result.get(2));
    }

    @Test
    void getTail_returnsEqualList_whenSizeIsEqualToListSize() {
        final List<Integer> list = List.of(0, 1, 2, 3, 4);
        final int size = 5;
        final List<Integer> result = CollectionsUtils.getTail(list, size);

        Assertions.assertEquals(list.size(), result.size());
        for (int i = 0; i < list.size(); i++) {
            Assertions.assertEquals(list.get(0), result.get(0));
        }
    }

    @Test
    void getTail_returnsEqualList_whenSizeIsGreaterThanListSize() {
        final List<Integer> list = List.of(0, 1, 2, 3, 4);
        final int size = 6;
        final List<Integer> result = CollectionsUtils.getTail(list, size);

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
                Arguments.of(List.of(), null),
                Arguments.of(List.of(1, 2, 3), 3)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLast")
    <T> void getLast_returnsLastItem_whenIterablesIsNotEmpty(Iterable<T> iterable, T expectedLastItem) {
        final T lastItem = CollectionsUtils.getLast(iterable);

        Assertions.assertEquals(expectedLastItem, lastItem);
    }

    // endregion

    // region insertInterpolated tests

    @Test
    void insertInterpolated_throwsIllegalArgumentException_whenIndexIsNegative() {
        final List<BigDecimal> list = List.of(DecimalUtils.ONE, DecimalUtils.setDefaultScale(10));
        final int index = -1;

        final Executable executable = () -> CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "index can't be negative");
    }

    @Test
    void insertInterpolated_throwsIllegalArgumentException_whenIndexIsGreaterThanListSize() {
        final List<BigDecimal> list = List.of(DecimalUtils.ONE, DecimalUtils.setDefaultScale(10));
        final int index = 3;

        final Executable executable = () -> CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "index can't be greater than size of list");
    }

    @Test
    void insertInterpolated_throwsIllegalArgumentException_whenListIsEmpty() {
        final List<BigDecimal> list = new ArrayList<>();
        final int index = 0;

        final Executable executable = () -> CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "list can't be empty");
    }

    @Test
    void insertInterpolated_addsElementToBeginning_whenIndexIsZero() {
        final List<BigDecimal> list = TestData.newBigDecimalList(5, 10);
        final int index = 0;

        CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage);

        Assertions.assertEquals(3, list.size());
        AssertUtils.assertEquals(5, list.get(0));
        AssertUtils.assertEquals(5, list.get(1));
        AssertUtils.assertEquals(10, list.get(2));
    }

    @Test
    void insertInterpolated_addsElementToEnd_whenIndexIsEqualsToListSize() {
        final List<BigDecimal> list = TestData.newBigDecimalList(5, 10);
        final int index = 2;

        CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage);

        Assertions.assertEquals(3, list.size());
        AssertUtils.assertEquals(5, list.get(0));
        AssertUtils.assertEquals(10, list.get(1));
        AssertUtils.assertEquals(10, list.get(2));
    }

    @Test
    void insertInterpolated_addsElementToMiddle_whenIndexIsInMiddle() {
        final List<BigDecimal> list = TestData.newBigDecimalList(5, 10, 0);
        final int index = 1;

        CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage);

        Assertions.assertEquals(4, list.size());
        AssertUtils.assertEquals(5, list.get(0));
        AssertUtils.assertEquals(7.5, list.get(1));
        AssertUtils.assertEquals(10, list.get(2));
        AssertUtils.assertEquals(0, list.get(3));
    }

    // endregion

    // region binarySearch tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forBinarySearch() {
        return Stream.of(
                Arguments.of(List.of(), 10, 0),
                Arguments.of(List.of(2, 3, 4, 5), 1, 0),
                Arguments.of(List.of(1, 2, 3, 4, 5), 1, 0),
                Arguments.of(List.of(1, 2, 3, 4, 5), 3, 2),
                Arguments.of(List.of(1, 2, 3, 4, 5), 5, 4),
                Arguments.of(List.of(1, 2, 4, 5), 3, 2),
                Arguments.of(List.of(1, 2, 3, 4, 5), 0, 0),
                Arguments.of(List.of(1, 2, 3, 4, 5), 6, 5)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forBinarySearch")
    void binarySearch(final List<Integer> list, final Integer key, final int expectedResult) {
        final int result = CollectionsUtils.binarySearch(list, key, Comparator.naturalOrder());

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion
}