package ru.obukhov.trader.common.util;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;
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

    static Stream<Arguments> getData_forGetAverage_withCollection() {
        return Stream.of(
                Arguments.of(ImmutableList.of(), 0.0),
                Arguments.of(ImmutableList.of(1000.0), 1000.0),
                Arguments.of(ImmutableList.of(100.0, 200.0, 1000.0), 433.33333)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverage_withCollection")
    void getAverage_withCollection(List<Double> values, Double expectedAverage) {
        List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        BigDecimal average = MathUtils.getAverage(bigDecimalValues);

        AssertUtils.assertEquals(expectedAverage, average);
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
                BigDecimal.valueOf(1000)
        );

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

}