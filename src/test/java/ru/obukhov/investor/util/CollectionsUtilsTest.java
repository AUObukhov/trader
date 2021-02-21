package ru.obukhov.investor.util;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.obukhov.investor.util.MathUtils.numbersEqual;

public class CollectionsUtilsTest {

    @Test
    public void reduceMultimap_average() {
        Multimap<String, BigDecimal> multimap = MultimapBuilder.hashKeys().arrayListValues().build();

        multimap.put("key1", BigDecimal.valueOf(10));

        multimap.put("key2", BigDecimal.valueOf(1));
        multimap.put("key2", BigDecimal.valueOf(2));
        multimap.put("key2", BigDecimal.valueOf(3));
        multimap.put("key2", BigDecimal.valueOf(4));

        Map<String, BigDecimal> result = CollectionsUtils.reduceMultimap(multimap, MathUtils::getAverage);

        assertTrue(numbersEqual(result.get("key1"), 10));
        assertTrue(numbersEqual(result.get("key2"), 2.5));
    }

    // region getTail tests

    @Test
    public void getTail_returnsEmptyList_whenSizeIsZero() {
        List<Integer> list = Arrays.asList(0, 1, 2, 3, 4);

        List<Integer> result = CollectionsUtils.getTail(list, 0);

        assertTrue(result.isEmpty());
    }

    @Test
    public void getTail_returnsTail_whenSizeIsLowerThanListSize() {
        List<Integer> list = Arrays.asList(0, 1, 2, 3, 4);
        int size = 3;
        List<Integer> result = CollectionsUtils.getTail(list, size);

        assertEquals(size, result.size());
        assertEquals(Integer.valueOf(2), result.get(0));
        assertEquals(Integer.valueOf(3), result.get(1));
        assertEquals(Integer.valueOf(4), result.get(2));
    }

    @Test
    public void getTail_returnsEqualList_whenSizeIsEqualToListSize() {
        List<Integer> list = Arrays.asList(0, 1, 2, 3, 4);
        int size = 5;
        List<Integer> result = CollectionsUtils.getTail(list, size);

        assertEquals(list.size(), result.size());
        for (int i = 0; i < list.size(); i++) {
            assertEquals(list.get(0), result.get(0));
        }
    }

    @Test
    public void getTail_returnsEqualList_whenSizeIsGreaterThanListSize() {
        List<Integer> list = Arrays.asList(0, 1, 2, 3, 4);
        int size = 6;
        List<Integer> result = CollectionsUtils.getTail(list, size);

        assertEquals(list.size(), result.size());
        for (int i = 0; i < list.size(); i++) {
            assertEquals(list.get(0), result.get(0));
        }
    }

    // endregion

    // region insertInterpolated tests

    @Test(expected = IllegalArgumentException.class)
    public void insertInterpolated_throwsIllegalArgumentException_whenIndexIsNegative() {
        List<BigDecimal> list = Arrays.asList(BigDecimal.ONE, BigDecimal.TEN);
        int index = -1;

        CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void insertInterpolated_throwsIllegalArgumentException_whenIndexIsGreaterThanListSize() {
        List<BigDecimal> list = Arrays.asList(BigDecimal.ONE, BigDecimal.TEN);
        int index = 3;

        CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void insertInterpolated_throwsIllegalArgumentException_whenListIsEmpty() {
        List<BigDecimal> list = new ArrayList<>();
        int index = 0;

        CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage);
    }

    @Test
    public void insertInterpolated_addsElementToBeginning_whenIndexIsZero() {
        List<BigDecimal> list = newArrayList(BigDecimal.valueOf(5), BigDecimal.TEN);
        int index = 0;

        CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage);

        assertEquals(3, list.size());
        assertTrue(MathUtils.numbersEqual(list.get(0), 5));
        assertTrue(MathUtils.numbersEqual(list.get(1), 5));
        assertTrue(MathUtils.numbersEqual(list.get(2), 10));
    }

    @Test
    public void insertInterpolated_addsElementToEnd_whenIndexIsEqualsToListSize() {
        List<BigDecimal> list = newArrayList(BigDecimal.valueOf(5), BigDecimal.TEN);
        int index = 2;

        CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage);

        assertEquals(3, list.size());
        assertTrue(MathUtils.numbersEqual(list.get(0), 5));
        assertTrue(MathUtils.numbersEqual(list.get(1), 10));
        assertTrue(MathUtils.numbersEqual(list.get(2), 10));
    }

    @Test
    public void insertInterpolated_addsElementToMiddle_whenIndexIsInMiddle() {
        List<BigDecimal> list = newArrayList(BigDecimal.valueOf(5), BigDecimal.TEN, BigDecimal.ZERO);
        int index = 1;

        CollectionsUtils.insertInterpolated(list, index, MathUtils::getAverage);

        assertEquals(4, list.size());
        assertTrue(MathUtils.numbersEqual(list.get(0), 5));
        assertTrue(MathUtils.numbersEqual(list.get(1), 7.5));
        assertTrue(MathUtils.numbersEqual(list.get(2), 10));
        assertTrue(MathUtils.numbersEqual(list.get(3), 0));
    }

    // endregion

    // region containsList tests

    @Test
    public void containsList_throwsIllegalArgumentException_whenListIsNull() {
        List<String> list = null;
        List<String> searchedList = Arrays.asList("0", "1");

        assertThrows(IllegalArgumentException.class,
                () -> CollectionsUtils.containsList(list, searchedList),
                "list must not be null");
    }

    @Test
    public void containsList_throwsIllegalArgumentException_whenSearchedListIsNull() {
        List<String> list = Arrays.asList("0", "1");
        List<String> searchedList = null;

        assertThrows(IllegalArgumentException.class,
                () -> CollectionsUtils.containsList(list, searchedList),
                "searchedList must not be null");
    }

    @Test
    public void containsList_returnTrue_whenSearchedListIsEmpty() {
        List<String> list = Arrays.asList("0", "1");
        List<String> searchedList = Collections.emptyList();

        assertTrue(CollectionsUtils.containsList(list, searchedList));
    }

    @Test
    public void containsList_returnTrue_whenBothListsAreEmpty() {
        List<String> list = Collections.emptyList();
        List<String> searchedList = Collections.emptyList();

        assertTrue(CollectionsUtils.containsList(list, searchedList));
    }

    @Test
    public void containsList_returnFalse_whenListIsEmpty() {
        List<String> list = Collections.emptyList();
        List<String> searchedList = Collections.singletonList("0");

        assertFalse(CollectionsUtils.containsList(list, searchedList));
    }

    @Test
    public void containsList_returnTrue_whenListsAreEqual() {
        List<String> list = Arrays.asList("0", "1");
        List<String> searchedList = Arrays.asList("0", "1");

        assertTrue(CollectionsUtils.containsList(list, searchedList));
    }

    @Test
    public void containsList_returnTrue_whenListContainsSearchedListFromTheBeginning() {
        List<String> list = Arrays.asList("0", "1", "2", "3");
        List<String> searchedList = Arrays.asList("0", "1");

        assertTrue(CollectionsUtils.containsList(list, searchedList));
    }

    @Test
    public void containsList_returnTrue_whenListContainsSearchedListInTheMiddle() {
        List<String> list = Arrays.asList("0", "1", "2", "3");
        List<String> searchedList = Arrays.asList("1", "2");

        assertTrue(CollectionsUtils.containsList(list, searchedList));
    }

    @Test
    public void containsList_returnTrue_whenListContainsSearchedListInTheEnd() {
        List<String> list = Arrays.asList("0", "1", "2", "3");
        List<String> searchedList = Arrays.asList("2", "3");

        assertTrue(CollectionsUtils.containsList(list, searchedList));
    }

    @Test
    public void containsList_returnFalse_whenListDoesNotContainsSearchedList() {
        List<String> list = Arrays.asList("0", "1", "2", "3");
        List<String> searchedList = Arrays.asList("5", "6");

        assertFalse(CollectionsUtils.containsList(list, searchedList));
    }

    @Test
    public void containsList_returnFalse_whenListContainsOnlyPartOfSearchedList() {
        List<String> list = Arrays.asList("0", "1", "2", "3");
        List<String> searchedList = Arrays.asList("2", "3", "4");

        assertFalse(CollectionsUtils.containsList(list, searchedList));
    }

    @Test
    public void containsList_returnFalse_whenListContainsSearchedListButInAnotherOrder() {
        List<String> list = Arrays.asList("0", "1", "2", "3");
        List<String> searchedList = Arrays.asList("2", "1");

        assertFalse(CollectionsUtils.containsList(list, searchedList));
    }

    // endregion

}