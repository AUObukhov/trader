package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.math.BigDecimal;
import java.util.stream.Stream;

class FinUtilsUnitTest {

    // region getRelativeProfit tests

    @Test
    void getRelativeProfit_throwsIllegalArgumentException_whenInvestmentIsNegative() {
        final Executable executable = () -> FinUtils.getRelativeProfit(BigDecimal.valueOf(-0.1), BigDecimal.TEN);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "investment can't be negative");
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetRelativeProfit() {
        return Stream.of(
                Arguments.of(0, 0, 0.0),
                Arguments.of(0, -10, 0.0),
                Arguments.of(0, 10, 0.0),
                Arguments.of(100, 10, 0.1),
                Arguments.of(30, 10, 0.33333)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetRelativeProfit")
    void getRelativeProfit(final double investment, final double profit, final double expectedRelativeProfit) {
        final BigDecimal investmentDecimal = DecimalUtils.setDefaultScale(investment);
        final BigDecimal profitDecimal = DecimalUtils.setDefaultScale(profit);

        final double relativeProfit = FinUtils.getRelativeProfit(investmentDecimal, profitDecimal);

        AssertUtils.assertEquals(expectedRelativeProfit, relativeProfit);
    }

    // endregion

    // region getAverageAnnualReturn tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetAverageAnnualReturn_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(0.9, 0.5, "daysCount can't be lower than 1"),
                Arguments.of(10, -1.1, "relativeProfit can't be lower than -1")
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverageAnnualReturn_throwsIllegalArgumentException")
    void getAverageAnnualReturn_throwsIllegalArgumentException(final double daysCount, final double relativeProfit, final String expectedMessage) {
        final Executable executable = () -> FinUtils.getAverageAnnualReturn(daysCount, relativeProfit);
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetAverageAnnualReturn() {
        return Stream.of(
                Arguments.of(DateUtils.DAYS_IN_YEAR / 2, -0.1, -0.19),
                Arguments.of(DateUtils.DAYS_IN_YEAR / 2, 0.1, 0.21),
                Arguments.of(DateUtils.DAYS_IN_YEAR / 2, 1.0, 3.0),
                Arguments.of(DateUtils.DAYS_IN_YEAR / 2, 1.5, 5.25),
                Arguments.of(DateUtils.DAYS_IN_YEAR * 2, -0.1, -0.05132),
                Arguments.of(DateUtils.DAYS_IN_YEAR * 2, 0.1, 0.04881),
                Arguments.of(DateUtils.DAYS_IN_YEAR * 2, 1.0, 0.41421),
                Arguments.of(DateUtils.DAYS_IN_YEAR * 2, 1.5, 0.58114)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverageAnnualReturn")
    void getAverageAnnualReturn(final double daysCount, final double relativeProfit, final double expectedAverageAnnualReturn) {
        final double averageAnnualReturn = FinUtils.getAverageAnnualReturn(daysCount, relativeProfit);

        AssertUtils.assertEquals(expectedAverageAnnualReturn, averageAnnualReturn);
    }

    // endregion

}