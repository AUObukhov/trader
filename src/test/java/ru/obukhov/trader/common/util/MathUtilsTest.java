package ru.obukhov.trader.common.util;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

        assertNull(max);
    }

    @Test
    void max_returnsMaxValue_whenValuesIsNotEmpty() {
        List<Double> values = Arrays.asList(-100d, 21d, 10d, 20d);

        Double max = MathUtils.max(values);

        assertEquals(max, Double.valueOf(21));
    }

    // endregion

    // region min tests

    @Test
    void min_returnsNull_whenValuesIsEmpty() {
        Double min = MathUtils.min(Collections.emptyList());

        assertNull(min);
    }

    @Test
    void min_returnsMaxValue_whenValuesIsNotEmpty() {
        List<Double> values = Arrays.asList(100d, -21d, 10d, 20d);

        Double min = MathUtils.min(values);

        assertEquals(min, Double.valueOf(-21));
    }

    // endregion

    // region getSimpleMovingAverages tests

    @Test
    void getSimpleMovingAverages_throwsIllegalArgumentException_whenPeriodIsLowerThanZero() {
        List<BigDecimal> values = Collections.emptyList();
        int period = -1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getSimpleMovingAverages(values, period),
                IllegalArgumentException.class,
                "period must be greater than zero");
    }

    @Test
    void getSimpleMovingAverages_throwsIllegalArgumentException_whenPeriodIsZero() {
        List<BigDecimal> values = Collections.emptyList();
        int period = 0;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getSimpleMovingAverages(values, period),
                IllegalArgumentException.class,
                "period must be greater than zero");
    }

    @Test
    void getSimpleMovingAverages_returnsEmptyList_whenValuesAreEmpty() {
        List<BigDecimal> values = Collections.emptyList();
        int period = 4;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, period);

        Assertions.assertTrue(movingAverages.isEmpty());
    }

    @Test
    void getSimpleMovingAverages_returnsEqualList_whenExistsSingleValue_andPeriodIsGreaterThanOne() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(1000));
        int period = 4;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, period);

        AssertUtils.assertListsAreEqual(values, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_returnsEqualList_whenPeriodIsEqualToValuesCount_andExistsSingleValue() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(1000));
        int period = 1;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, period);

        AssertUtils.assertListsAreEqual(values, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_returnsAveragesList_whenPeriodIsEqualToValuesCount() {
        List<BigDecimal> values = Arrays.asList(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000)
        );
        int period = 4;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, period);

        List<BigDecimal> expectedAverages = Arrays.asList(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(2500)
        );
        AssertUtils.assertListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_returnsAveragesList_whenPeriodIsGreaterThanValuesCount() {
        List<BigDecimal> values = Arrays.asList(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000)
        );
        int period = 5;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, period);

        List<BigDecimal> expectedAverages = Arrays.asList(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(2000)
        );
        AssertUtils.assertListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_returnsEqualList_whenPeriodIsOne_andExistsSeveralValues() {
        List<BigDecimal> values = Arrays.asList(
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
        int period = 1;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, period);

        AssertUtils.assertListsAreEqual(values, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_returnsMovingAveragesEqualList_whenPeriodIsLowerThanValuesCount() {
        List<BigDecimal> values = Arrays.asList(
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
        int period = 4;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, period);

        List<BigDecimal> expectedAverages = Arrays.asList(
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
        AssertUtils.assertListsAreEqual(expectedAverages, movingAverages);
    }

    // endregion

    // region getLinearWeightedMovingAverages tests

    @Test
    void getLinearWeightedMovingAverages_throwsIllegalArgumentException_whenPeriodIsLowerThanZero() {
        List<BigDecimal> values = Collections.emptyList();
        int period = -1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getLinearWeightedMovingAverages(values, period),
                IllegalArgumentException.class,
                "period must be greater than zero");
    }

    @Test
    void getLinearWeightedMovingAverages_throwsIllegalArgumentException_whenPeriodIsZero() {
        List<BigDecimal> values = Collections.emptyList();
        int period = 0;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getLinearWeightedMovingAverages(values, period),
                IllegalArgumentException.class,
                "period must be greater than zero");
    }

    @Test
    void getLinearWeightedMovingAverages_returnsEmptyList_whenValuesAreEmpty() {
        List<BigDecimal> values = Collections.emptyList();
        int period = 4;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, period);

        Assertions.assertTrue(movingAverages.isEmpty());
    }

    @Test
    void getLinearWeightedMovingAverages_returnsEqualList_whenExistsSingleValue_andPeriodIsGreaterThanOne() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(1000));
        int period = 4;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, period);

        AssertUtils.assertListsAreEqual(values, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_returnsEqualList_whenPeriodIsEqualToValuesCount_andExistsSingleValue() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(1000));
        int period = 1;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, period);

        AssertUtils.assertListsAreEqual(values, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_returnsAveragesList_whenPeriodIsEqualToValuesCount() {
        List<BigDecimal> values = Arrays.asList(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000)
        );
        int period = 4;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, period);

        List<BigDecimal> expectedAverages = Arrays.asList(
                BigDecimal.valueOf(1000),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(5000.0 / 3)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(14000.0 / 6)),
                BigDecimal.valueOf(3000)
        );
        AssertUtils.assertListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_returnsAveragesList_whenPeriodIsGreaterThanValuesCount() {
        List<BigDecimal> values = Arrays.asList(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000)
        );
        int period = 5;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, period);

        List<BigDecimal> expectedAverages = Arrays.asList(
                BigDecimal.valueOf(1000),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(5000.0 / 3)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(14000.0 / 6))
        );
        AssertUtils.assertListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_returnsEqualList_whenPeriodIsOne_andExistsSeveralValues() {
        List<BigDecimal> values = Arrays.asList(
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
        int period = 1;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, period);

        AssertUtils.assertListsAreEqual(values, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_returnsMovingAveragesEqualList_whenPeriodIsLowerThanValuesCount() {
        List<BigDecimal> values = Arrays.asList(
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
        int period = 4;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, period);

        List<BigDecimal> expectedAverages = Arrays.asList(
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
        AssertUtils.assertListsAreEqual(expectedAverages, movingAverages);
    }

    // endregion

    // region getExponentialWeightedMovingAverages tests

    @Test
    void getExponentialWeightedMovingAverages_throwsIllegalArgumentException_whenWeightDecreaseIsLowerThanZero() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = -0.1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_throwsIllegalArgumentException_whenWeightDecreaseIsZero() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 0.0;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_throwsIllegalArgumentException_whenWeightDecreaseIsGreaterThanOne() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 1.1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_returnsEmptyList_whenValuesAreEmpty() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 0.8;

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease);

        Assertions.assertTrue(movingAverages.isEmpty());
    }

    @Test
    void getExponentialWeightedMovingAverages_returnsEqualList_whenExistsSingleValue() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(1000));
        double weightDecrease = 0.8;

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(values, weightDecrease);

        AssertUtils.assertListsAreEqual(values, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_returnsEqualsList_whenWeightDecreaseIsOne() {
        List<BigDecimal> values = Arrays.asList(
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

        AssertUtils.assertListsAreEqual(values, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_returnsAveragesListWithDefaultScale() {
        List<BigDecimal> values = Arrays.asList(
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
    void getExponentialWeightedMovingAverages_returnsEqualList_whenAllValuesAreEqual() {
        List<BigDecimal> values = Arrays.asList(
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

        AssertUtils.assertListsAreEqual(values, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_returnsAveragesList() {
        List<BigDecimal> values = Arrays.asList(
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

        List<BigDecimal> expectedAverages = Arrays.asList(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1800),
                BigDecimal.valueOf(2760),
                BigDecimal.valueOf(3752),
                BigDecimal.valueOf(4750.4),
                BigDecimal.valueOf(5750.08),
                BigDecimal.valueOf(6750.016),
                BigDecimal.valueOf(7750.0032),
                BigDecimal.valueOf(8750.00064),
                BigDecimal.valueOf(9750.00013)
        );
        AssertUtils.assertListsAreEqual(expectedAverages, movingAverages);
    }

    // endregion

}