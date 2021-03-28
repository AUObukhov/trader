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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

class MathUtilsTest {

    private static final Function<BigDecimal, BigDecimal> SELF = number -> number;

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

    // region getSimpleMovingAverages tests

    @Test
    void getSimpleMovingAverages_throwsIllegalArgumentException_whenWindowIsLowerThanZero() {
        List<BigDecimal> values = Collections.emptyList();
        int window = -1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getSimpleMovingAverages(values, SELF, window),
                IllegalArgumentException.class,
                "window must be positive");
    }

    @Test
    void getSimpleMovingAverages_throwsIllegalArgumentException_whenWindowIsZero() {
        List<BigDecimal> values = Collections.emptyList();
        int window = 0;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getSimpleMovingAverages(values, SELF, window),
                IllegalArgumentException.class,
                "window must be positive");
    }

    @Test
    void getSimpleMovingAverages_returnsEmptyList_whenValuesAreEmpty() {
        List<BigDecimal> values = Collections.emptyList();
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, SELF, window);

        Assertions.assertTrue(movingAverages.isEmpty());
    }

    @Test
    void getSimpleMovingAverages_returnsEqualList_whenExistsSingleValue_andWindowIsGreaterThanOne() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(1000));
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, SELF, window);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_returnsEqualList_whenWindowIsEqualToValuesCount_andExistsSingleValue() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(1000));
        int window = 1;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, SELF, window);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_returnsAveragesList_whenWindowIsEqualToValuesCount() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000)
        );
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, SELF, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(2500)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_returnsAveragesList_whenWindowIsGreaterThanValuesCount() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000)
        );
        int window = 5;

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, SELF, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(2000)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_returnsEqualList_whenWindowIsOne_andExistsSeveralValues() {
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

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, SELF, window);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getSimpleMovingAverages_returnsAverages_whenWindowIsLowerThanValuesCount() {
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

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(values, SELF, window);

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

    // region getLinearWeightedMovingAverages tests

    @Test
    void getLinearWeightedMovingAverages_throwsIllegalArgumentException_whenWindowIsLowerThanZero() {
        List<BigDecimal> values = Collections.emptyList();
        int window = -1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getLinearWeightedMovingAverages(values, SELF, window),
                IllegalArgumentException.class,
                "window must be positive");
    }

    @Test
    void getLinearWeightedMovingAverages_throwsIllegalArgumentException_whenWindowIsZero() {
        List<BigDecimal> values = Collections.emptyList();
        int window = 0;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getLinearWeightedMovingAverages(values, SELF, window),
                IllegalArgumentException.class,
                "window must be positive");
    }

    @Test
    void getLinearWeightedMovingAverages_returnsEmptyList_whenValuesAreEmpty() {
        List<BigDecimal> values = Collections.emptyList();
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, SELF, window);

        Assertions.assertTrue(movingAverages.isEmpty());
    }

    @Test
    void getLinearWeightedMovingAverages_returnsEqualList_whenExistsSingleValue_andWindowIsGreaterThanOne() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(1000));
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, SELF, window);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_returnsEqualList_whenWindowIsEqualToValuesCount_andExistsSingleValue() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(1000));
        int window = 1;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, SELF, window);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_returnsAveragesList_whenWindowIsEqualToValuesCount() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(4000)
        );
        int window = 4;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, SELF, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(5000.0 / 3)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(14000.0 / 6)),
                BigDecimal.valueOf(3000)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_returnsAveragesList_whenWindowIsGreaterThanValuesCount() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(3000)
        );
        int window = 5;

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, SELF, window);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(5000.0 / 3)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(14000.0 / 6))
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_returnsEqualList_whenWindowIsOne_andExistsSeveralValues() {
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

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, SELF, window);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getLinearWeightedMovingAverages_returnsMovingAveragesEqualList_whenWindowIsLowerThanValuesCount() {
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

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(values, SELF, window);

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

    // region getExponentialWeightedMovingAverages tests

    @Test
    void getExponentialWeightedMovingAverages_throwsIllegalArgumentException_whenWeightDecreaseIsLowerThanZero() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = -0.1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(values, SELF, weightDecrease),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_throwsIllegalArgumentException_whenWeightDecreaseIsZero() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 0.0;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(values, SELF, weightDecrease),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_throwsIllegalArgumentException_whenWeightDecreaseIsGreaterThanOne() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 1.1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(values, SELF, weightDecrease),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAverages_returnsEmptyList_whenValuesAreEmpty() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 0.8;

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(values, SELF, weightDecrease);

        Assertions.assertTrue(movingAverages.isEmpty());
    }

    @Test
    void getExponentialWeightedMovingAverages_returnsEqualList_whenExistsSingleValue() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(1000));
        double weightDecrease = 0.8;

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(values, SELF, weightDecrease);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_returnsEqualsList_whenWeightDecreaseIsOne() {
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

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(values, SELF, weightDecrease);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_returnsAveragesListWithDefaultScale() {
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

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(values, SELF, weightDecrease);

        for (BigDecimal average : movingAverages) {
            Assertions.assertTrue(DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages");
        }
    }

    @Test
    void getExponentialWeightedMovingAverages_returnsEqualList_whenAllValuesAreEqual() {
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

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(values, SELF, weightDecrease);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAverages_returnsAveragesList() {
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

        List<BigDecimal> movingAverages = MathUtils.getExponentialWeightedMovingAverages(values, SELF, weightDecrease);

        List<BigDecimal> expectedAverages = ImmutableList.of(
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
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    // endregion

    // region getExponentialWeightedMovingAveragesOfArbitraryOrder tests

    @Test
    void getExponentialWeightedMovingAveragesOfArbitraryOrder_throwsIllegalArgumentException_whenWeightDecreaseIsLowerThanZero() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = -0.1;
        int order = 3;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder(values, SELF, weightDecrease, order),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAveragesOfArbitraryOrder_throwsIllegalArgumentException_whenWeightDecreaseIsZero() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 0.0;
        int order = 3;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder(values, SELF, weightDecrease, order),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAveragesOfArbitraryOrder_throwsIllegalArgumentException_whenWeightDecreaseIsGreaterThanOne() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 1.1;
        int order = 3;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder(values, SELF, weightDecrease, order),
                IllegalArgumentException.class,
                "weightDecrease must be in range (0; 1]");
    }

    @Test
    void getExponentialWeightedMovingAveragesOfArbitraryOrder_throwsIllegalArgumentException_whenOrderIsLowerThanZero() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 0.5;
        int order = -1;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder(values, SELF, weightDecrease, order),
                IllegalArgumentException.class,
                "order must be positive");
    }

    @Test
    void getExponentialWeightedMovingAveragesOfArbitraryOrder_throwsIllegalArgumentException_whenOrderIsZero() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 0.5;
        int order = 0;

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder(values, SELF, weightDecrease, order),
                IllegalArgumentException.class,
                "order must be positive");
    }

    @Test
    void getExponentialWeightedMovingAveragesOfArbitraryOrder_returnsEmptyList_whenValuesAreEmpty() {
        List<BigDecimal> values = Collections.emptyList();
        double weightDecrease = 0.8;
        int order = 3;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder(values, SELF, weightDecrease, order);

        Assertions.assertTrue(movingAverages.isEmpty());
    }

    @Test
    void getExponentialWeightedMovingAveragesOfArbitraryOrder_returnsEqualList_whenExistsSingleValue() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(1000));
        double weightDecrease = 0.8;
        int order = 3;

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder(values, SELF, weightDecrease, order);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAveragesOfArbitraryOrder_returnsEqualsList_whenWeightDecreaseIsOne() {
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
                MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder(values, SELF, weightDecrease, order);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAveragesOfArbitraryOrder_returnsAveragesListWithDefaultScale() {
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
                MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder(values, SELF, weightDecrease, order);

        for (BigDecimal average : movingAverages) {
            Assertions.assertTrue(DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages");
        }
    }

    @Test
    void getExponentialWeightedMovingAveragesOfArbitraryOrder_returnsEqualList_whenAllValuesAreEqual() {
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
                MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder(values, SELF, weightDecrease, order);

        AssertUtils.assertBigDecimalListsAreEqual(values, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAveragesOfArbitraryOrder_returnsAveragesList_forFirstOrder() {
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
                MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder(values, SELF, weightDecrease, order);

        List<BigDecimal> expectedAverages = ImmutableList.of(
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
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAveragesOfArbitraryOrder_returnsAveragesList_forSecondOrder() {
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
                MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder(values, SELF, weightDecrease, order);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1640),
                BigDecimal.valueOf(2536),
                BigDecimal.valueOf(3508.8),
                BigDecimal.valueOf(4502.08),
                BigDecimal.valueOf(5500.48),
                BigDecimal.valueOf(6500.1088),
                BigDecimal.valueOf(7500.02432),
                BigDecimal.valueOf(8500.00538),
                BigDecimal.valueOf(9500.00118)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    @Test
    void getExponentialWeightedMovingAveragesOfArbitraryOrder_returnsAveragesList_forThirdOrder() {
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
                MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder(values, SELF, weightDecrease, order);

        List<BigDecimal> expectedAverages = ImmutableList.of(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1512),
                BigDecimal.valueOf(2331.2),
                BigDecimal.valueOf(3273.28),
                BigDecimal.valueOf(4256.32),
                BigDecimal.valueOf(5251.648),
                BigDecimal.valueOf(6250.41664),
                BigDecimal.valueOf(7250.10278),
                BigDecimal.valueOf(8250.02486),
                BigDecimal.valueOf(9250.00591)
        );
        AssertUtils.assertBigDecimalListsAreEqual(expectedAverages, movingAverages);
    }

    // endregion

    // region getLocalMaximums tests

    @Test
    void getLocalExtremes_returnsEmptyList_whenValuesIsEmpty_andNaturalOrder() {
        List<BigDecimal> values = Collections.emptyList();

        List<Integer> extremes = MathUtils.getLocalExtremes(
                values,
                (BigDecimal number) -> number,
                Comparator.naturalOrder()
        );

        Assertions.assertTrue(extremes.isEmpty());
    }

    @Test
    void getLocalExtremes_returnsSingleZeroIndex_whenThereIsSingleValue_andNaturalOrder() {
        List<BigDecimal> values = Collections.singletonList(BigDecimal.valueOf(100));

        List<Integer> extremes = MathUtils.getLocalExtremes(
                values,
                (BigDecimal number) -> number,
                Comparator.naturalOrder()
        );

        List<Integer> expectedExtremes = Collections.singletonList(0);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_returnsSingleZeroIndex_whenThereAreTwoValues_andFirstIsGreater_andNaturalOrder() {
        List<BigDecimal> values = ImmutableList.of(BigDecimal.valueOf(100), BigDecimal.valueOf(90));

        List<Integer> extremes = MathUtils.getLocalExtremes(
                values,
                (BigDecimal number) -> number,
                Comparator.naturalOrder()
        );

        List<Integer> expectedExtremes = Collections.singletonList(0);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_returnsSingleOneIndex_whenThereAreTwoEqualsValues_andNaturalOrder() {
        List<BigDecimal> values = ImmutableList.of(BigDecimal.valueOf(100), BigDecimal.valueOf(100));

        List<Integer> extremes = MathUtils.getLocalExtremes(
                values,
                (BigDecimal number) -> number,
                Comparator.naturalOrder()
        );

        List<Integer> expectedExtremes = Collections.singletonList(1);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_returnsSingleOneIndex_whenThereAreTwoValues_andSecondIsGreater_andNaturalOrder() {
        List<BigDecimal> values = ImmutableList.of(
                BigDecimal.valueOf(90),
                BigDecimal.valueOf(100)
        );

        List<Integer> extremes = MathUtils.getLocalExtremes(
                values,
                (BigDecimal number) -> number,
                Comparator.naturalOrder()
        );

        List<Integer> expectedExtremes = Collections.singletonList(1);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_returnsIndexOfLastElement_whenThereAreMultipleEqualValues_andNaturalOrder() {
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

        List<Integer> extremes = MathUtils.getLocalExtremes(
                values,
                (BigDecimal number) -> number,
                Comparator.naturalOrder()
        );

        List<Integer> expectedExtremes = Collections.singletonList(9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder1() {
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

        List<Integer> extremes = MathUtils.getLocalExtremes(
                values,
                (BigDecimal number) -> number,
                Comparator.naturalOrder()
        );

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 8);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder2() {
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

        List<Integer> extremes = MathUtils.getLocalExtremes(
                values,
                (BigDecimal number) -> number,
                Comparator.naturalOrder()
        );

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 8);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder3() {
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

        List<Integer> extremes = MathUtils.getLocalExtremes(
                values,
                (BigDecimal number) -> number,
                Comparator.naturalOrder()
        );

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder4() {
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

        List<Integer> extremes = MathUtils.getLocalExtremes(
                values,
                (BigDecimal number) -> number,
                Comparator.naturalOrder()
        );

        List<Integer> expectedExtremes = ImmutableList.of(0, 2, 5, 9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_returnsProperIndices_whenThereAreMultipleValues_andNaturalOrder5() {
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

        List<Integer> extremes = MathUtils.getLocalExtremes(
                values,
                (BigDecimal number) -> number,
                Comparator.naturalOrder()
        );

        List<Integer> expectedExtremes = ImmutableList.of(0, 3, 7, 9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder1() {
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

        List<Integer> extremes = MathUtils.getLocalExtremes(
                values,
                (BigDecimal number) -> number,
                Comparator.reverseOrder()
        );

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 8);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder2() {
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

        List<Integer> extremes = MathUtils.getLocalExtremes(
                values,
                (BigDecimal number) -> number,
                Comparator.reverseOrder()
        );

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 8);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder3() {
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

        List<Integer> extremes = MathUtils.getLocalExtremes(
                values,
                (BigDecimal number) -> number,
                Comparator.reverseOrder()
        );

        List<Integer> expectedExtremes = ImmutableList.of(2, 5, 9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @Test
    void getLocalExtremes_returnsProperIndices_whenThereAreMultipleValues_andReverseOrder4() {
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

        List<Integer> extremes = MathUtils.getLocalExtremes(
                values,
                (BigDecimal number) -> number,
                Comparator.reverseOrder()
        );

        List<Integer> expectedExtremes = ImmutableList.of(0, 2, 5, 9);
        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    // endregion

}