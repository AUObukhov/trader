package ru.obukhov.trader.common.util;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

class MathUtilsTest {

    // region getAverage with collection tests

    @Test
    void getAverage_withCollection_returnsZero_whenCollectionIsEmpty() {

        List<BigDecimal> numbers = ImmutableList.of();

        BigDecimal average = MathUtils.getAverage(numbers);

        AssertUtils.assertEquals(BigDecimal.ZERO, average);

    }

    @Test
    void getAverage_withCollection_returnsNumber_whenItIsSingleInCollection() {

        List<BigDecimal> numbers = ImmutableList.of(BigDecimal.TEN);

        BigDecimal average = MathUtils.getAverage(numbers);

        AssertUtils.assertEquals(numbers.get(0), average);

    }

    @Test
    void getAverage_withCollection_returnsAverage_whenMultipleNumbersInCollection() {

        List<BigDecimal> numbers = ImmutableList.of(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(1000)
        );

        BigDecimal average = MathUtils.getAverage(numbers);

        AssertUtils.assertEquals(BigDecimal.valueOf(433.33333), average);

    }

    // endregion

    // region getAverage with VarArgs tests

    @Test
    void getAverage_withVarArgs_returnsZero_whenNoArguments() {

        BigDecimal average = MathUtils.getAverage();

        AssertUtils.assertEquals(BigDecimal.ZERO, average);

    }

    @Test
    void getAverage_withVarArgs_returnsNumber_whenSingleArguments() {

        BigDecimal number = BigDecimal.TEN;

        BigDecimal average = MathUtils.getAverage(number);

        AssertUtils.assertEquals(number, average);

    }

    @Test
    void getAverage_withVarArgs_returnsAverage_whenMultipleNumbersInCollection() {


        BigDecimal average = MathUtils.getAverage(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(1000));

        AssertUtils.assertEquals(BigDecimal.valueOf(433.33333), average);

    }

    // endregion

    // region getWeightedAverage tests

    @Test
    void getWeightedAverage_returnsZero_whenCollectionIsEmpty() {
        SortedMap<OffsetDateTime, BigDecimal> dateTimesToAmounts = new TreeMap<>();
        OffsetDateTime endTime = DateUtils.getDateTime(2021, 3, 10, 11, 12, 13);

        BigDecimal weightedAverage = MathUtils.getWeightedAverage(dateTimesToAmounts, endTime);

        AssertUtils.assertEquals(BigDecimal.ZERO, weightedAverage);
    }

    @Test
    void getWeightedAverage_returnsProperValue_whenCollectionIsNotEmpty() {
        SortedMap<OffsetDateTime, BigDecimal> dateTimesToAmounts = new TreeMap<>();
        dateTimesToAmounts.put(
                DateUtils.getDateTime(2021, 1, 1, 10, 0, 0),
                BigDecimal.valueOf(100000)
        );
        dateTimesToAmounts.put(
                DateUtils.getDateTime(2021, 2, 1, 10, 0, 0),
                BigDecimal.valueOf(110000)
        );
        dateTimesToAmounts.put(
                DateUtils.getDateTime(2021, 3, 1, 10, 0, 0),
                BigDecimal.valueOf(120000)
        );
        OffsetDateTime endTime = DateUtils.getDateTime(2021, 3, 10, 10, 0, 0);

        BigDecimal weightedAverage = MathUtils.getWeightedAverage(dateTimesToAmounts, endTime);

        AssertUtils.assertEquals(BigDecimal.valueOf(106764.70588), weightedAverage);
    }

    // endregion

    // region max tests

    @Test
    void max_returnsNull_whenValuesIsEmpty() {
        Double max = MathUtils.max(Collections.emptyList());

        Assertions.assertNull(max);
    }

    @Test
    void max_returnsMaxValue_whenValuesIsNotEmpty() {
        List<Double> values = ImmutableList.of(-100d, 21d, 10d, 20d);

        Double max = MathUtils.max(values);

        Assertions.assertEquals(max, Double.valueOf(21));
    }

    // endregion

    // region min tests

    @Test
    void min_returnsNull_whenValuesIsEmpty() {
        Double min = MathUtils.min(Collections.emptyList());

        Assertions.assertNull(min);
    }

    @Test
    void min_returnsMaxValue_whenValuesIsNotEmpty() {
        List<Double> values = ImmutableList.of(100d, -21d, 10d, 20d);

        Double min = MathUtils.min(values);

        Assertions.assertEquals(min, Double.valueOf(-21));
    }

    // endregion

    // region getSimpleMovingAverages with valueExtractor tests

    @Test
    void getSimpleMovingAverages_withValueExtractor_throwsIllegalArgumentException_whenWindowIsNegative() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();
        int window = -1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getSimpleMovingAverages(elements, Optional::get, window),
                IllegalArgumentException.class,
                "window must be positive"
        );
    }

    @Test
    void getSimpleMovingAverages_withValueExtractor_throwsIllegalArgumentException_whenWindowIsZero() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();
        int window = 0;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getSimpleMovingAverages(elements, Optional::get, window),
                IllegalArgumentException.class,
                "window must be positive"
        );
    }

    @Test
    void getSimpleMovingAverages_withValueExtractor_returnsEmptyList_whenValuesAreEmpty() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(elements, Optional::get, window);

        Assertions.assertTrue(movingAverages.isEmpty());
    }

    @Test
    void getSimpleMovingAverages_withValueExtractor_returnsEqualList_whenExistsSingleValue_andWindowIsGreaterThanOne() {
        List<Optional<BigDecimal>> elements = Collections.singletonList(Optional.of(BigDecimal.valueOf(1000)));
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(elements, Optional::get, window);

        List<BigDecimal> expectedAverages = Collections.singletonList(BigDecimal.valueOf(1000));
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_withValueExtractor_returnsEqualList_whenWindowIsEqualToValuesCount_andExistsSingleValue() {
        List<Optional<BigDecimal>> elements = Collections.singletonList(Optional.of(BigDecimal.valueOf(1000)));
        int window = 1;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(elements, Optional::get, window);

        List<BigDecimal> expectedAverages = Collections.singletonList(BigDecimal.valueOf(1000));
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_withValueExtractor_returnsAveragesList_whenWindowIsEqualToValuesCount() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(2000)),
                Optional.of(BigDecimal.valueOf(3000)),
                Optional.of(BigDecimal.valueOf(4000))
        );
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(elements, Optional::get, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(2500)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_withValueExtractor_returnsAveragesList_whenWindowIsGreaterThanValuesCount() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(2000)),
                Optional.of(BigDecimal.valueOf(3000))
        );
        int window = 5;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(elements, Optional::get, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(2000)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_withValueExtractor_returnsEqualList_whenWindowIsOne_andExistsSeveralValues() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(2000)),
                Optional.of(BigDecimal.valueOf(3000)),
                Optional.of(BigDecimal.valueOf(4000)),
                Optional.of(BigDecimal.valueOf(5000)),
                Optional.of(BigDecimal.valueOf(6000)),
                Optional.of(BigDecimal.valueOf(7000)),
                Optional.of(BigDecimal.valueOf(8000)),
                Optional.of(BigDecimal.valueOf(9000)),
                Optional.of(BigDecimal.valueOf(10000))
        );
        int window = 1;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(elements, Optional::get, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(10000)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_withValueExtractor_returnsAverages_whenWindowIsLowerThanValuesCount() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(2000)),
                Optional.of(BigDecimal.valueOf(3000)),
                Optional.of(BigDecimal.valueOf(4000)),
                Optional.of(BigDecimal.valueOf(5000)),
                Optional.of(BigDecimal.valueOf(6000)),
                Optional.of(BigDecimal.valueOf(7000)),
                Optional.of(BigDecimal.valueOf(8000)),
                Optional.of(BigDecimal.valueOf(9000)),
                Optional.of(BigDecimal.valueOf(10000))
        );
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(elements, Optional::get, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(2500),
                BigDecimal.valueOf(3500),
                BigDecimal.valueOf(4500),
                BigDecimal.valueOf(5500),
                BigDecimal.valueOf(6500),
                BigDecimal.valueOf(7500),
                BigDecimal.valueOf(8500)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    // endregion

    // region getSimpleMovingAverages without valueExtractor tests

    @Test
    void getSimpleMovingAverages_withoutValueExtractor_throwsIllegalArgumentException_whenWindowIsNegative() {
        List<BigDecimal> values = Collections.emptyList();
        int window = -1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getSimpleMovingAverages(values, window),
                IllegalArgumentException.class,
                "window must be positive");
    }

    @Test
    void getSimpleMovingAverages_withoutValueExtractor_throwsIllegalArgumentException_whenWindowIsZero() {
        List<BigDecimal> values = Collections.emptyList();
        int window = 0;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getSimpleMovingAverages(values, window),
                IllegalArgumentException.class,
                "window must be positive");
    }

    @Test
    void getSimpleMovingAverages_withoutValueExtractor_returnsEmptyList_whenValuesAreEmpty() {
        List<BigDecimal> values = Collections.emptyList();
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, window);

        Assertions.assertTrue(movingAverages.isEmpty());
    }

    @Test
    void getSimpleMovingAverages_withoutValueExtractor_returnsEqualList_whenExistsSingleValue_andWindowIsGreaterThanOne() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(1000));
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, window);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_withoutValueExtractor_returnsEqualList_whenWindowIsEqualToValuesCount_andExistsSingleValue() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(1000));
        int window = 1;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, window);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_withoutValueExtractor_returnsAveragesList_whenWindowIsEqualToValuesCount() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000)
        );
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(2500)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_withoutValueExtractor_returnsAveragesList_whenWindowIsGreaterThanValuesCount() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000)
        );
        int window = 5;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(2000)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_withoutValueExtractor_returnsEqualList_whenWindowIsOne_andExistsSeveralValues() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(10000)
        );
        int window = 1;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, window);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_withoutValueExtractor_returnsAverages_whenWindowIsLowerThanValuesCount() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(10000)
        );
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(2500),
                BigDecimal.valueOf(3500),
                BigDecimal.valueOf(4500),
                BigDecimal.valueOf(5500),
                BigDecimal.valueOf(6500),
                BigDecimal.valueOf(7500),
                BigDecimal.valueOf(8500)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    // endregion

    // region getLinearWeightedMovingAverages with ValueExtractor tests

    @Test
    void getLinearWeightedMovingAverages_withValueExtractor_throwsIllegalArgumentException_whenWindowIsNegative() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();
        int window = -1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getLinearWeightedMovingAverages(elements, Optional::get, window),
                IllegalArgumentException.class,
                "window must be positive"
        );
    }

    @Test
    void getLinearWeightedMovingAverages_withValueExtractor_throwsIllegalArgumentException_whenWindowIsZero() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();
        int window = 0;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getLinearWeightedMovingAverages(elements, Optional::get, window),
                IllegalArgumentException.class,
                "window must be positive"
        );
    }

    @Test
    void getLinearWeightedMovingAverages_withValueExtractor_returnsEmptyList_whenValuesAreEmpty() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(elements, Optional::get, window);

        Assertions.assertTrue(movingAverages.isEmpty());
    }

    @Test
    void getLinearWeightedMovingAverages_withValueExtractor_returnsEqualList_whenExistsSingleValue_andWindowIsGreaterThanOne() {
        List<Optional<BigDecimal>> elements = Collections.singletonList(Optional.of(BigDecimal.valueOf(1000)));
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(elements, Optional::get, window);

        List<BigDecimal> expectedAverages = Collections.singletonList(BigDecimal.valueOf(1000));
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_withValueExtractor_returnsEqualList_whenWindowIsEqualToValuesCount_andExistsSingleValue() {
        List<Optional<BigDecimal>> elements = Collections.singletonList(Optional.of(BigDecimal.valueOf(1000)));
        int window = 1;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(elements, Optional::get, window);

        List<BigDecimal> expectedAverages = Collections.singletonList(BigDecimal.valueOf(1000));
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_withValueExtractor_returnsAveragesList_whenWindowIsEqualToValuesCount() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(2000)),
                Optional.of(BigDecimal.valueOf(3000)),
                Optional.of(BigDecimal.valueOf(4000))
        );
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(elements, Optional::get, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(5000.0 / 3)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(14000.0 / 6)),
                BigDecimal.valueOf(3000)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_withValueExtractor_returnsAveragesList_whenWindowIsGreaterThanValuesCount() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(2000)),
                Optional.of(BigDecimal.valueOf(3000))
        );
        int window = 5;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(elements, Optional::get, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(5000.0 / 3)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(14000.0 / 6))
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_withValueExtractor_returnsEqualList_whenWindowIsOne_andExistsSeveralValues() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(2000)),
                Optional.of(BigDecimal.valueOf(3000)),
                Optional.of(BigDecimal.valueOf(4000)),
                Optional.of(BigDecimal.valueOf(5000)),
                Optional.of(BigDecimal.valueOf(6000)),
                Optional.of(BigDecimal.valueOf(7000)),
                Optional.of(BigDecimal.valueOf(8000)),
                Optional.of(BigDecimal.valueOf(9000)),
                Optional.of(BigDecimal.valueOf(10000))
        );
        int window = 1;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(elements, Optional::get, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(10000)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_withValueExtractor_returnsMovingAveragesEqualList_whenWindowIsLowerThanValuesCount() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(2000)),
                Optional.of(BigDecimal.valueOf(3000)),
                Optional.of(BigDecimal.valueOf(4000)),
                Optional.of(BigDecimal.valueOf(5000)),
                Optional.of(BigDecimal.valueOf(6000)),
                Optional.of(BigDecimal.valueOf(7000)),
                Optional.of(BigDecimal.valueOf(8000)),
                Optional.of(BigDecimal.valueOf(9000)),
                Optional.of(BigDecimal.valueOf(10000))
        );
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(elements, Optional::get, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(5000.0 / 3)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(14000.0 / 6)),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    // endregion

    // region getLinearWeightedMovingAverages without ValueExtractor tests

    @Test
    void getLinearWeightedMovingAverages_withoutValueExtractor_throwsIllegalArgumentException_whenWindowIsNegative() {
        List<BigDecimal> values = Collections.emptyList();
        int window = -1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getLinearWeightedMovingAverages(values, window),
                IllegalArgumentException.class,
                "window must be positive");
    }

    @Test
    void getLinearWeightedMovingAverages_withoutValueExtractor_throwsIllegalArgumentException_whenWindowIsZero() {
        List<BigDecimal> values = Collections.emptyList();
        int window = 0;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getLinearWeightedMovingAverages(values, window),
                IllegalArgumentException.class,
                "window must be positive");
    }

    @Test
    void getLinearWeightedMovingAverages_withoutValueExtractor_returnsEmptyList_whenValuesAreEmpty() {
        List<BigDecimal> values = Collections.emptyList();
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, window);

        Assertions.assertTrue(movingAverages.isEmpty());
    }

    @Test
    void getLinearWeightedMovingAverages_withoutValueExtractor_returnsEqualList_whenExistsSingleValue_andWindowIsGreaterThanOne() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(1000));
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, window);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_withoutValueExtractor_returnsEqualList_whenWindowIsEqualToValuesCount_andExistsSingleValue() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(1000));
        int window = 1;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, window);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_withoutValueExtractor_returnsAveragesList_whenWindowIsEqualToValuesCount() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000)
        );
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(5000.0 / 3)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(14000.0 / 6)),
                BigDecimal.valueOf(3000)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_withoutValueExtractor_returnsAveragesList_whenWindowIsGreaterThanValuesCount() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000)
        );
        int window = 5;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(5000.0 / 3)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(14000.0 / 6))
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_withoutValueExtractor_returnsEqualList_whenWindowIsOne_andExistsSeveralValues() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(10000)
        );
        int window = 1;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, window);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_withoutValueExtractor_returnsMovingAveragesEqualList_whenWindowIsLowerThanValuesCount() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(10000)
        );
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(5000.0 / 3)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(14000.0 / 6)),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    // endregion

    // region getExponentialWeightedMovingAverages with valueExtractor and without order tests

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithoutOrder_throwsIllegalArgumentException_whenWeightDecreaseIsNegative() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();
        double weightDecrease = -0.1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithoutOrder_throwsIllegalArgumentException_whenWeightDecreaseIsZero() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();
        double weightDecrease = 0.0;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithoutOrder_throwsIllegalArgumentException_whenWeightDecreaseIsGreaterThanOne() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();
        double weightDecrease = 1.1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithoutOrder_returnsEmptyList_whenValuesAreEmpty() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();
        double weightDecrease = 0.8;

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease);

        Assertions.assertTrue(movingAverages.isEmpty());
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithoutOrder_returnsEqualList_whenExistsSingleValue() {
        List<Optional<BigDecimal>> elements = Collections.singletonList(Optional.of(BigDecimal.valueOf(1000)));
        double weightDecrease = 0.8;

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease);

        List<BigDecimal> expectedAverages = ImmutableList.of(BigDecimal.valueOf(1000));
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithoutOrder_returnsEqualsList_whenWeightDecreaseIsOne() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(2000)),
                Optional.of(BigDecimal.valueOf(3000)),
                Optional.of(BigDecimal.valueOf(4000)),
                Optional.of(BigDecimal.valueOf(5000)),
                Optional.of(BigDecimal.valueOf(6000)),
                Optional.of(BigDecimal.valueOf(7000)),
                Optional.of(BigDecimal.valueOf(8000)),
                Optional.of(BigDecimal.valueOf(9000)),
                Optional.of(BigDecimal.valueOf(10000))
        );
        double weightDecrease = 1.0;

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(10000)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithoutOrder_returnsAveragesListWithDefaultScale() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(2000)),
                Optional.of(BigDecimal.valueOf(3000)),
                Optional.of(BigDecimal.valueOf(4000)),
                Optional.of(BigDecimal.valueOf(5000)),
                Optional.of(BigDecimal.valueOf(6000)),
                Optional.of(BigDecimal.valueOf(7000)),
                Optional.of(BigDecimal.valueOf(8000)),
                Optional.of(BigDecimal.valueOf(9000)),
                Optional.of(BigDecimal.valueOf(10000))
        );
        double weightDecrease = 0.8;

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease);

        for (BigDecimal average : movingAverages) {
            Assertions.assertTrue(DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages");
        }
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithoutOrder_returnsEqualList_whenAllValuesAreEqual() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000))
        );
        double weightDecrease = 0.8;

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithoutOrder_returnsAveragesList() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(2000)),
                Optional.of(BigDecimal.valueOf(3000)),
                Optional.of(BigDecimal.valueOf(4000)),
                Optional.of(BigDecimal.valueOf(5000)),
                Optional.of(BigDecimal.valueOf(6000)),
                Optional.of(BigDecimal.valueOf(7000)),
                Optional.of(BigDecimal.valueOf(8000)),
                Optional.of(BigDecimal.valueOf(9000)),
                Optional.of(BigDecimal.valueOf(10000))
        );
        double weightDecrease = 0.8;

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1124.99994),
                BigDecimal.valueOf(2024.99968),
                BigDecimal.valueOf(3004.99840),
                BigDecimal.valueOf(4000.99200),
                BigDecimal.valueOf(5000.16000),
                BigDecimal.valueOf(5999.84000),
                BigDecimal.valueOf(6999.00800),
                BigDecimal.valueOf(7995.00160),
                BigDecimal.valueOf(8975.00032),
                BigDecimal.valueOf(9875.00006)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    // endregion

    // region getExponentialWeightedMovingAverages with valueExtractor and with order tests

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithOrder_throwsIllegalArgumentException_whenWeightDecreaseIsNegative() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();
        double weightDecrease = -0.1;
        int order = 3;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease, order),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithOrder_throwsIllegalArgumentException_whenWeightDecreaseIsZero() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();
        double weightDecrease = 0.0;
        int order = 3;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease, order),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithOrder_throwsIllegalArgumentException_whenWeightDecreaseIsGreaterThanOne() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();
        double weightDecrease = 1.1;
        int order = 3;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease, order),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithOrder_throwsIllegalArgumentException_whenOrderIsNegative() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();
        double weightDecrease = 0.5;
        int order = -1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease, order),
                IllegalArgumentException.class,
                "order must be positive");
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithOrder_throwsIllegalArgumentException_whenOrderIsZero() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();
        double weightDecrease = 0.5;
        int order = 0;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease, order),
                IllegalArgumentException.class,
                "order must be positive");
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithOrder_returnsEmptyList_whenValuesAreEmpty() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();
        double weightDecrease = 0.8;
        int order = 3;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease, order);

        Assertions.assertTrue(movingAverages.isEmpty());
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithOrder_returnsEqualList_whenExistsSingleValue() {
        List<Optional<BigDecimal>> elements = Collections.singletonList(Optional.of(BigDecimal.valueOf(1000)));
        double weightDecrease = 0.8;
        int order = 3;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease, order);

        List<BigDecimal> expectedAverages = ImmutableList.of(BigDecimal.valueOf(1000));
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithOrder_returnsEqualsList_whenWeightDecreaseIsOne() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(2000)),
                Optional.of(BigDecimal.valueOf(3000)),
                Optional.of(BigDecimal.valueOf(4000)),
                Optional.of(BigDecimal.valueOf(5000)),
                Optional.of(BigDecimal.valueOf(6000)),
                Optional.of(BigDecimal.valueOf(7000)),
                Optional.of(BigDecimal.valueOf(8000)),
                Optional.of(BigDecimal.valueOf(9000)),
                Optional.of(BigDecimal.valueOf(10000))
        );
        double weightDecrease = 1.0;
        int order = 3;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease, order);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(10000)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithOrder_returnsAveragesListWithDefaultScale() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(2000)),
                Optional.of(BigDecimal.valueOf(3000)),
                Optional.of(BigDecimal.valueOf(4000)),
                Optional.of(BigDecimal.valueOf(5000)),
                Optional.of(BigDecimal.valueOf(6000)),
                Optional.of(BigDecimal.valueOf(7000)),
                Optional.of(BigDecimal.valueOf(8000)),
                Optional.of(BigDecimal.valueOf(9000)),
                Optional.of(BigDecimal.valueOf(10000))
        );
        double weightDecrease = 0.8;
        int order = 3;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease, order);

        for (BigDecimal average : movingAverages) {
            Assertions.assertTrue(DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages");
        }
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithOrder_returnsEqualList_whenAllValuesAreEqual() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(1000))
        );
        double weightDecrease = 0.8;
        int order = 3;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease, order);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithOrder_returnsAveragesList_forFirstOrder() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(2000)),
                Optional.of(BigDecimal.valueOf(3000)),
                Optional.of(BigDecimal.valueOf(4000)),
                Optional.of(BigDecimal.valueOf(5000)),
                Optional.of(BigDecimal.valueOf(6000)),
                Optional.of(BigDecimal.valueOf(7000)),
                Optional.of(BigDecimal.valueOf(8000)),
                Optional.of(BigDecimal.valueOf(9000)),
                Optional.of(BigDecimal.valueOf(10000))
        );
        double weightDecrease = 0.8;
        int order = 1;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease, order);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1124.99994),
                BigDecimal.valueOf(2024.99968),
                BigDecimal.valueOf(3004.99840),
                BigDecimal.valueOf(4000.99200),
                BigDecimal.valueOf(5000.16000),
                BigDecimal.valueOf(5999.84000),
                BigDecimal.valueOf(6999.00800),
                BigDecimal.valueOf(7995.00160),
                BigDecimal.valueOf(8975.00032),
                BigDecimal.valueOf(9875.00006)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithOrder_returnsAveragesList_forSecondOrder() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(2000)),
                Optional.of(BigDecimal.valueOf(3000)),
                Optional.of(BigDecimal.valueOf(4000)),
                Optional.of(BigDecimal.valueOf(5000)),
                Optional.of(BigDecimal.valueOf(6000)),
                Optional.of(BigDecimal.valueOf(7000)),
                Optional.of(BigDecimal.valueOf(8000)),
                Optional.of(BigDecimal.valueOf(9000)),
                Optional.of(BigDecimal.valueOf(10000))
        );
        double weightDecrease = 0.8;
        int order = 2;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease, order);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1249.99941),
                BigDecimal.valueOf(2069.99731),
                BigDecimal.valueOf(3017.98784),
                BigDecimal.valueOf(4004.34560),
                BigDecimal.valueOf(5000.80000),
                BigDecimal.valueOf(5999.20000),
                BigDecimal.valueOf(6995.65440),
                BigDecimal.valueOf(7982.01216),
                BigDecimal.valueOf(8930.00269),
                BigDecimal.valueOf(9750.00059)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_withValueExtractor_andWithOrder_returnsAveragesList_forThirdOrder() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(1000)),
                Optional.of(BigDecimal.valueOf(2000)),
                Optional.of(BigDecimal.valueOf(3000)),
                Optional.of(BigDecimal.valueOf(4000)),
                Optional.of(BigDecimal.valueOf(5000)),
                Optional.of(BigDecimal.valueOf(6000)),
                Optional.of(BigDecimal.valueOf(7000)),
                Optional.of(BigDecimal.valueOf(8000)),
                Optional.of(BigDecimal.valueOf(9000)),
                Optional.of(BigDecimal.valueOf(10000))
        );
        double weightDecrease = 0.8;
        int order = 3;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease, order);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1374.99704),
                BigDecimal.valueOf(2130.98757),
                BigDecimal.valueOf(3040.54861),
                BigDecimal.valueOf(4011.43168),
                BigDecimal.valueOf(5002.33600),
                BigDecimal.valueOf(5997.66400),
                BigDecimal.valueOf(6988.56832),
                BigDecimal.valueOf(7959.45139),
                BigDecimal.valueOf(8869.01243),
                BigDecimal.valueOf(9625.00296)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    // endregion

    // region getExponentialWeightedMovingAverages without valueExtractor and without order tests

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithoutOrder_throwsIllegalArgumentException_whenWeightDecreaseIsNegative() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = -0.1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithoutOrder_throwsIllegalArgumentException_whenWeightDecreaseIsZero() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 0.0;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithoutOrder_throwsIllegalArgumentException_whenWeightDecreaseIsGreaterThanOne() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 1.1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithoutOrder_returnsEmptyList_whenValuesAreEmpty() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 0.8;

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease);

        Assertions.assertTrue(movingAverages.isEmpty());
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithoutOrder_returnsEqualList_whenExistsSingleValue() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(1000));
        double weightDecrease = 0.8;

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithoutOrder_returnsEqualsList_whenWeightDecreaseIsOne() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(10000)
        );
        double weightDecrease = 1.0;

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithoutOrder_returnsAveragesListWithDefaultScale() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(10000)
        );
        double weightDecrease = 0.8;

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease);

        for (BigDecimal average : movingAverages) {
            Assertions.assertTrue(DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages");
        }
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithoutOrder_returnsEqualList_whenAllValuesAreEqual() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000)
        );
        double weightDecrease = 0.8;

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithoutOrder_returnsAveragesList() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(10000)
        );
        double weightDecrease = 0.8;

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1124.99994),
                BigDecimal.valueOf(2024.99968),
                BigDecimal.valueOf(3004.99840),
                BigDecimal.valueOf(4000.99200),
                BigDecimal.valueOf(5000.16000),
                BigDecimal.valueOf(5999.84000),
                BigDecimal.valueOf(6999.00800),
                BigDecimal.valueOf(7995.00160),
                BigDecimal.valueOf(8975.00032),
                BigDecimal.valueOf(9875.00006)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    // endregion

    // region getExponentialWeightedMovingAverages without valueExtractor and with order tests

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithOrder_throwsIllegalArgumentException_whenWeightDecreaseIsNegative() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = -0.1;
        int order = 3;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease, order),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithOrder_throwsIllegalArgumentException_whenWeightDecreaseIsZero() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 0.0;
        int order = 3;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease, order),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithOrder_throwsIllegalArgumentException_whenWeightDecreaseIsGreaterThanOne() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 1.1;
        int order = 3;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease, order),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithOrder_throwsIllegalArgumentException_whenOrderIsNegative() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 0.5;
        int order = -1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease, order),
                IllegalArgumentException.class,
                "order must be positive");
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithOrder_throwsIllegalArgumentException_whenOrderIsZero() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 0.5;
        int order = 0;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease, order),
                IllegalArgumentException.class,
                "order must be positive");
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithOrder_returnsEmptyList_whenValuesAreEmpty() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 0.8;
        int order = 3;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease, order);

        Assertions.assertTrue(movingAverages.isEmpty());
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithOrder_returnsEqualList_whenExistsSingleValue() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(1000));
        double weightDecrease = 0.8;
        int order = 3;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease, order);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithOrder_returnsEqualsList_whenWeightDecreaseIsOne() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(10000)
        );
        double weightDecrease = 1.0;
        int order = 3;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease, order);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithOrder_returnsAveragesListWithDefaultScale() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(10000)
        );
        double weightDecrease = 0.8;
        int order = 3;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease, order);

        for (BigDecimal average : movingAverages) {
            Assertions.assertTrue(DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages");
        }
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithOrder_returnsEqualList_whenAllValuesAreEqual() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000)
        );
        double weightDecrease = 0.8;
        int order = 3;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease, order);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithOrder_returnsAveragesList_forFirstOrder() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(10000)
        );
        double weightDecrease = 0.8;
        int order = 1;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease, order);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1124.99994),
                BigDecimal.valueOf(2024.99968),
                BigDecimal.valueOf(3004.99840),
                BigDecimal.valueOf(4000.99200),
                BigDecimal.valueOf(5000.16000),
                BigDecimal.valueOf(5999.84000),
                BigDecimal.valueOf(6999.00800),
                BigDecimal.valueOf(7995.00160),
                BigDecimal.valueOf(8975.00032),
                BigDecimal.valueOf(9875.00006)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithOrder_returnsAveragesList_forSecondOrder() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(10000)
        );
        double weightDecrease = 0.8;
        int order = 2;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease, order);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1249.99941),
                BigDecimal.valueOf(2069.99731),
                BigDecimal.valueOf(3017.98784),
                BigDecimal.valueOf(4004.34560),
                BigDecimal.valueOf(5000.80000),
                BigDecimal.valueOf(5999.20000),
                BigDecimal.valueOf(6995.65440),
                BigDecimal.valueOf(7982.01216),
                BigDecimal.valueOf(8930.00269),
                BigDecimal.valueOf(9750.00059)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_withoutValueExtractor_andWithOrder_returnsAveragesList_forThirdOrder() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(10000)
        );
        double weightDecrease = 0.8;
        int order = 3;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease, order);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1374.99704),
                BigDecimal.valueOf(2130.98757),
                BigDecimal.valueOf(3040.54861),
                BigDecimal.valueOf(4011.43168),
                BigDecimal.valueOf(5002.33600),
                BigDecimal.valueOf(5997.66400),
                BigDecimal.valueOf(6988.56832),
                BigDecimal.valueOf(7959.45139),
                BigDecimal.valueOf(8869.01243),
                BigDecimal.valueOf(9625.00296)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    // endregion

    // region getLocalExtremes with valueExtractor tests

    @Test
    void getLocalExtremes_withValueExtractor_returnsEmptyList_whenValuesIsEmpty_andNaturalOrder() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();

        List<Integer> extremes = MathUtils.getLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        Assertions.assertTrue(extremes.isEmpty());
    }

    @Test
    void getLocalExtremes_withValueExtractor_returnsSingleZeroIndex_whenThereIsSingleValue_andNaturalOrder() {
        List<Optional<BigDecimal>> elements = Collections.singletonList(Optional.of(BigDecimal.valueOf(100)));

        List<Integer> extremes = MathUtils.getLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(0);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withValueExtractor_returnsSingleZeroIndex_whenThereAreTwoValues_andFirstIsGreater_andNaturalOrder() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(90))
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(0);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withValueExtractor_returnsSingleOneIndex_whenThereAreTwoEqualsValues_andNaturalOrder() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100))
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(1);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withValueExtractor_returnsSingleOneIndex_whenThereAreTwoValues_andSecondIsGreater_andNaturalOrder() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(90)),
                Optional.of(BigDecimal.valueOf(100))
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(1);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withValueExtractor_returnsIndexOfLastElement_whenThereAreMultipleEqualValues_andNaturalOrder() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100))
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder1() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(29.9)),
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(25)),
                Optional.of(BigDecimal.valueOf(21)),
                Optional.of(BigDecimal.valueOf(15)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(30))
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 8);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder2() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(29.9)),
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(25)),
                Optional.of(BigDecimal.valueOf(21)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(30))
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 8);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder3() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(29.9)),
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(25)),
                Optional.of(BigDecimal.valueOf(21)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(50))
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder4() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(29.9)),
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(25)),
                Optional.of(BigDecimal.valueOf(21)),
                Optional.of(BigDecimal.valueOf(15)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(50.1))
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(0, 2, 5, 9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder5() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(5)),
                Optional.of(BigDecimal.valueOf(5)),
                Optional.of(BigDecimal.valueOf(5.1)),
                Optional.of(BigDecimal.valueOf(4)),
                Optional.of(BigDecimal.valueOf(3.5)),
                Optional.of(BigDecimal.valueOf(5)),
                Optional.of(BigDecimal.valueOf(70)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(80))
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(0, 3, 7, 9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder1() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(5)),
                Optional.of(BigDecimal.valueOf(5)),
                Optional.of(BigDecimal.valueOf(5.1)),
                Optional.of(BigDecimal.valueOf(4)),
                Optional.of(BigDecimal.valueOf(3.5)),
                Optional.of(BigDecimal.valueOf(5)),
                Optional.of(BigDecimal.valueOf(70)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(80))
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(elements, Optional::get, Comparator.reverseOrder());

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 8);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder2() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(15)),
                Optional.of(BigDecimal.valueOf(15)),
                Optional.of(BigDecimal.valueOf(15.1)),
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(19)),
                Optional.of(BigDecimal.valueOf(21)),
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(30))
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(elements, Optional::get, Comparator.reverseOrder());

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 8);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder3() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(15)),
                Optional.of(BigDecimal.valueOf(15)),
                Optional.of(BigDecimal.valueOf(15.1)),
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(19)),
                Optional.of(BigDecimal.valueOf(21)),
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(10))
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(elements, Optional::get, Comparator.reverseOrder());

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder4() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(30.1)),
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(19)),
                Optional.of(BigDecimal.valueOf(21)),
                Optional.of(BigDecimal.valueOf(60)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(49.9))
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(elements, Optional::get, Comparator.reverseOrder());

        List<Integer> expectedExtremes = ImmutableList.of(0, 2, 5, 9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    // endregion

    // region getLocalExtremes without valueExtractor tests

    @Test
    void getLocalExtremes_withoutValueExtractor_returnsEmptyList_whenValuesIsEmpty_andNaturalOrder() {
        List<BigDecimal> values = Collections.emptyList();

        List<Integer> extremes = MathUtils.getLocalExtremes(values, Comparator.naturalOrder());

        Assertions.assertTrue(extremes.isEmpty());
    }

    @Test
    void getLocalExtremes_withoutValueExtractor_returnsSingleZeroIndex_whenThereIsSingleValue_andNaturalOrder() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(100));

        List<Integer> extremes = MathUtils.getLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(0);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withoutValueExtractor_returnsSingleZeroIndex_whenThereAreTwoValues_andFirstIsGreater_andNaturalOrder() {
        List<BigDecimal> values = ImmutableList.of(BigDecimal.valueOf(100), BigDecimal.valueOf(90));

        List<Integer> extremes = MathUtils.getLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(0);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withoutValueExtractor_returnsSingleOneIndex_whenThereAreTwoEqualsValues_andNaturalOrder() {
        List<BigDecimal> values = ImmutableList.of(BigDecimal.valueOf(100), BigDecimal.valueOf(100));

        List<Integer> extremes = MathUtils.getLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(1);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withoutValueExtractor_returnsSingleOneIndex_whenThereAreTwoValues_andSecondIsGreater_andNaturalOrder() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(90),
                BigDecimal.valueOf(100)
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(1);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withoutValueExtractor_returnsIndexOfLastElement_whenThereAreMultipleEqualValues_andNaturalOrder() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100)
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder1() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(29.9),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(30)
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 8);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder2() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(29.9),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(30)
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 8);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder3() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(29.9),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(50)
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder4() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(29.9),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(50.1)
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(0, 2, 5, 9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder5() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(5.1),
                BigDecimal.valueOf(4),
                BigDecimal.valueOf(3.5),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(70),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(80)
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(0, 3, 7, 9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder1() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(5.1),
                BigDecimal.valueOf(4),
                BigDecimal.valueOf(3.5),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(70),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(80)
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(values, Comparator.reverseOrder());

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 8);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder2() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(15.1),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(19),
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(30)
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(values, Comparator.reverseOrder());

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 8);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder3() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(15.1),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(19),
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(10)
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(values, Comparator.reverseOrder());

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder4() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(30.1),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(19),
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(60),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(49.9)
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(values, Comparator.reverseOrder());

        List<Integer> expectedExtremes = ImmutableList.of(0, 2, 5, 9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    // endregion

    // region getSortedLocalExtremes with valueExtractor tests

    @Test
    void getSortedLocalExtremes_withValueExtractor_returnsEmptyList_whenValuesIsEmpty_andNaturalOrder() {
        List<Optional<BigDecimal>> elements = Collections.emptyList();

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        Assertions.assertTrue(extremes.isEmpty());
    }

    @Test
    void getSortedLocalExtremes_withValueExtractor_returnsSingleZeroIndex_whenThereIsSingleValue_andNaturalOrder() {
        List<Optional<BigDecimal>> elements = Collections.singletonList(Optional.of(BigDecimal.valueOf(100)));

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(0);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withValueExtractor_returnsSingleZeroIndex_whenThereAreTwoValues_andFirstIsGreater_andNaturalOrder() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(90))
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(0);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withValueExtractor_returnsSingleOneIndex_whenThereAreTwoEqualsValues_andNaturalOrder() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100))
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(1);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withValueExtractor_returnsSingleOneIndex_whenThereAreTwoValues_andSecondIsGreater_andNaturalOrder() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(90)),
                Optional.of(BigDecimal.valueOf(100))
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(1);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withValueExtractor_returnsIndexOfLastElement_whenThereAreMultipleEqualValues_andNaturalOrder() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(100))
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder1() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(29.9)),
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(25)),
                Optional.of(BigDecimal.valueOf(21)),
                Optional.of(BigDecimal.valueOf(15)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(30))
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(8, 2, 5);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder2() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(29.9)),
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(25)),
                Optional.of(BigDecimal.valueOf(21)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(30))
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(8, 2, 5);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder3() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(29.9)),
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(25)),
                Optional.of(BigDecimal.valueOf(21)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(50))
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(9, 2, 5);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder4() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(100)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(29.9)),
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(25)),
                Optional.of(BigDecimal.valueOf(21)),
                Optional.of(BigDecimal.valueOf(15)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(50.1))
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(0, 9, 2, 5);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder5() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(5)),
                Optional.of(BigDecimal.valueOf(5)),
                Optional.of(BigDecimal.valueOf(5.1)),
                Optional.of(BigDecimal.valueOf(4)),
                Optional.of(BigDecimal.valueOf(3.5)),
                Optional.of(BigDecimal.valueOf(5)),
                Optional.of(BigDecimal.valueOf(70)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(80))
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(elements, Optional::get, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(9, 7, 0, 3);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder1() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(5)),
                Optional.of(BigDecimal.valueOf(5)),
                Optional.of(BigDecimal.valueOf(5.1)),
                Optional.of(BigDecimal.valueOf(4)),
                Optional.of(BigDecimal.valueOf(3.5)),
                Optional.of(BigDecimal.valueOf(5)),
                Optional.of(BigDecimal.valueOf(70)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(80))
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(elements, Optional::get, Comparator.reverseOrder());

        List<Integer> expectedExtremes = ImmutableList.of(5, 2, 8);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder2() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(15)),
                Optional.of(BigDecimal.valueOf(15)),
                Optional.of(BigDecimal.valueOf(15.1)),
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(19)),
                Optional.of(BigDecimal.valueOf(21)),
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(30))
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(elements, Optional::get, Comparator.reverseOrder());

        List<Integer> expectedExtremes = ImmutableList.of(8, 2, 5);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder3() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(15)),
                Optional.of(BigDecimal.valueOf(15)),
                Optional.of(BigDecimal.valueOf(15.1)),
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(19)),
                Optional.of(BigDecimal.valueOf(21)),
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(10))
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(elements, Optional::get, Comparator.reverseOrder());

        List<Integer> expectedExtremes = ImmutableList.of(9, 2, 5);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder4() {
        List<Optional<BigDecimal>> elements = ImmutableList.of(
                Optional.of(BigDecimal.valueOf(10)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(30)),
                Optional.of(BigDecimal.valueOf(30.1)),
                Optional.of(BigDecimal.valueOf(20)),
                Optional.of(BigDecimal.valueOf(19)),
                Optional.of(BigDecimal.valueOf(21)),
                Optional.of(BigDecimal.valueOf(60)),
                Optional.of(BigDecimal.valueOf(50)),
                Optional.of(BigDecimal.valueOf(49.9))
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(elements, Optional::get, Comparator.reverseOrder());

        List<Integer> expectedExtremes = ImmutableList.of(0, 5, 2, 9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    // endregion

    // region getSortedLocalExtremes without valueExtractor tests

    @Test
    void getSortedLocalExtremes_withoutValueExtractor_returnsEmptyList_whenValuesIsEmpty_andNaturalOrder() {
        List<BigDecimal> values = Collections.emptyList();

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(values, Comparator.naturalOrder());

        Assertions.assertTrue(extremes.isEmpty());
    }

    @Test
    void getSortedLocalExtremes_withoutValueExtractor_returnsSingleZeroIndex_whenThereIsSingleValue_andNaturalOrder() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(100));

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(0);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withoutValueExtractor_returnsSingleZeroIndex_whenThereAreTwoValues_andFirstIsGreater_andNaturalOrder() {
        List<BigDecimal> values = ImmutableList.of(BigDecimal.valueOf(100), BigDecimal.valueOf(90));

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(0);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withoutValueExtractor_returnsSingleOneIndex_whenThereAreTwoEqualsValues_andNaturalOrder() {
        List<BigDecimal> values = ImmutableList.of(BigDecimal.valueOf(100), BigDecimal.valueOf(100));

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(1);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withoutValueExtractor_returnsSingleOneIndex_whenThereAreTwoValues_andSecondIsGreater_andNaturalOrder() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(90),
                BigDecimal.valueOf(100)
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(1);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withoutValueExtractor_returnsIndexOfLastElement_whenThereAreMultipleEqualValues_andNaturalOrder() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100)
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = Collections.singletonList(9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder1() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(29.9),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(30)
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(8, 2, 5);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder2() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(29.9),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(30)
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(8, 2, 5);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder3() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(29.9),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(50)
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(9, 2, 5);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder4() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(29.9),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(50.1)
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(0, 9, 2, 5);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder5() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(5.1),
                BigDecimal.valueOf(4),
                BigDecimal.valueOf(3.5),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(70),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(80)
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(values, Comparator.naturalOrder());

        List<Integer> expectedExtremes = ImmutableList.of(9, 7, 0, 3);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder1() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(5.1),
                BigDecimal.valueOf(4),
                BigDecimal.valueOf(3.5),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(70),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(80)
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(values, Comparator.reverseOrder());

        List<Integer> expectedExtremes = ImmutableList.of(5, 2, 8);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder2() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(15.1),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(19),
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(30)
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(values, Comparator.reverseOrder());

        List<Integer> expectedExtremes = ImmutableList.of(8, 2, 5);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder3() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(15.1),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(19),
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(10)
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(values, Comparator.reverseOrder());

        List<Integer> expectedExtremes = ImmutableList.of(9, 2, 5);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getSortedLocalExtremes_withoutValueExtractor_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder4() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(30.1),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(19),
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(60),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(49.9)
        );

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(values, Comparator.reverseOrder());

        List<Integer> expectedExtremes = ImmutableList.of(0, 5, 2, 9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    // endregion

}