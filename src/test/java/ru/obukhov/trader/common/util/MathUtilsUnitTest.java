package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.TestDataHelper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

class MathUtilsUnitTest {

    // region getAverage with collection tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetAverage_withCollection() {
        return Stream.of(
                Arguments.of(List.of(), 0.0),
                Arguments.of(List.of(1000.0), 1000.0),
                Arguments.of(List.of(100.0, 200.0, 1000.0), 433.33333)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverage_withCollection")
    void getAverage_withCollection(final List<Double> values, final Double expectedAverage) {
        final List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        final BigDecimal average = MathUtils.getAverage(bigDecimalValues);

        AssertUtils.assertEquals(expectedAverage, average);
    }

    // endregion

    // region getAverage with VarArgs tests

    @Test
    void getAverage_withVarArgs_returnsZero_whenNoArguments() {
        final BigDecimal average = MathUtils.getAverage();

        AssertUtils.assertEquals(0, average);
    }

    @Test
    void getAverage_withVarArgs_returnsNumber_whenSingleArguments() {
        final BigDecimal number = BigDecimal.TEN;

        final BigDecimal average = MathUtils.getAverage(number);

        AssertUtils.assertEquals(number, average);
    }

    @Test
    void getAverage_withVarArgs_returnsAverage_whenMultipleNumbersInCollection() {
        final BigDecimal average = MathUtils.getAverage(BigDecimal.valueOf(100), BigDecimal.valueOf(200), BigDecimal.valueOf(1000));

        AssertUtils.assertEquals(433.33333, average);
    }

    // endregion

    // region getWeightedAverage tests

    @Test
    void getWeightedAverage_returnsZero_whenCollectionIsEmpty() {
        final SortedMap<OffsetDateTime, BigDecimal> dateTimesToAmounts = new TreeMap<>();
        final OffsetDateTime endTime = DateTimeTestData.createDateTime(2021, 3, 10, 11, 12, 13);

        final BigDecimal weightedAverage = MathUtils.getWeightedAverage(dateTimesToAmounts, endTime);

        AssertUtils.assertEquals(0, weightedAverage);
    }

    @Test
    void getWeightedAverage_returnsProperValue_whenCollectionIsNotEmpty() {
        final SortedMap<OffsetDateTime, BigDecimal> dateTimesToAmounts = new TreeMap<>();
        dateTimesToAmounts.put(DateTimeTestData.createDateTime(2021, 1, 1, 10), BigDecimal.valueOf(100000));
        dateTimesToAmounts.put(DateTimeTestData.createDateTime(2021, 2, 1, 10), BigDecimal.valueOf(110000));
        dateTimesToAmounts.put(DateTimeTestData.createDateTime(2021, 3, 1, 10), BigDecimal.valueOf(120000));
        final OffsetDateTime endTime = DateTimeTestData.createDateTime(2021, 3, 10, 10);

        final BigDecimal weightedAverage = MathUtils.getWeightedAverage(dateTimesToAmounts, endTime);

        AssertUtils.assertEquals(106764.70588, weightedAverage);
    }

    // endregion

    // region max tests

    @Test
    void max_returnsNull_whenValuesIsEmpty() {
        final Double max = MathUtils.max(Collections.emptyList());

        Assertions.assertNull(max);
    }

    @Test
    void max_returnsMaxValue_whenValuesIsNotEmpty() {
        final List<Double> values = List.of(-100d, 21d, 10d, 20d);

        Double max = MathUtils.max(values);

        Assertions.assertEquals(max, Double.valueOf(21));
    }

    // endregion

    // region min tests

    @Test
    void min_returnsNull_whenValuesIsEmpty() {
        final Double min = MathUtils.min(Collections.emptyList());

        Assertions.assertNull(min);
    }

    @Test
    void min_returnsMaxValue_whenValuesIsNotEmpty() {
        final List<Double> values = List.of(100d, -21d, 10d, 20d);

        final Double min = MathUtils.min(values);

        Assertions.assertEquals(min, Double.valueOf(-21));
    }

    // endregion

}