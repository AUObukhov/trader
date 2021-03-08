package ru.obukhov.investor.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MathUtils {

    public static final int DEFAULT_SCALE = 5;

    // region arithmetics

    /**
     * @return average value of passed collection with scale = {@link MathUtils#DEFAULT_SCALE} and rounding mode = Half Up
     */
    public static BigDecimal getAverage(Collection<BigDecimal> numbers) {
        return numbers.stream()
                .reduce(BigDecimal::add)
                .map(sum -> divide(sum, numbers.size()))
                .orElse(BigDecimal.ZERO);
    }

    /**
     * @return average value of passed {@code numbers} with scale = {@link MathUtils#DEFAULT_SCALE} and rounding mode = Half Up
     */
    public static BigDecimal getAverage(BigDecimal... numbers) {
        return getAverage(Arrays.asList(numbers));
    }

    /**
     * @return multiplier * multiplicand
     */
    public static BigDecimal multiply(BigDecimal multiplier, double multiplicand) {
        return multiplier.multiply(BigDecimal.valueOf(multiplicand));
    }

    /**
     * @return result of division of {@code dividend} by {@code divisor} with scale = {@link MathUtils#DEFAULT_SCALE} and rounding mode = Half Up
     */
    public static BigDecimal divide(BigDecimal dividend, int divisor) {
        return divide(dividend, BigDecimal.valueOf(divisor));
    }

    /**
     * @return result of division of {@code dividend} by {@code divisor} with scale = {@link MathUtils#DEFAULT_SCALE} and rounding mode = Half Up
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        return dividend.divide(divisor, DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * @return integer quotient of division of {@code dividend} by {@code divisor}
     */
    public static int getIntegerQuotient(BigDecimal dividend, BigDecimal divisor) {
        return dividend.divide(divisor, RoundingMode.DOWN).intValue();
    }

    /**
     * @return {@code number} * {@code fraction}
     */
    public static BigDecimal getFraction(BigDecimal number, double fraction) {
        return multiply(number, fraction);
    }

    /**
     * @return {@code number} * (1 + {@code fraction})
     */
    public static BigDecimal addFraction(BigDecimal number, double fraction) {
        return multiply(number, 1 + fraction);
    }

    /**
     * @return {@code number} * (1 - {@code fraction})
     */
    public static BigDecimal subtractFraction(BigDecimal number, double fraction) {
        return multiply(number, 1 - fraction);
    }

    /**
     * @return {@code number1} / {@code number2} - 1
     */
    public static BigDecimal getFractionDifference(BigDecimal number1, BigDecimal number2) {
        return divide(number1, number2).subtract(BigDecimal.ONE);
    }

    /**
     * @return BigDecimal with scale equal to minimum non negative value between scale of given {@code number}, 0 and
     * {@link MathUtils#DEFAULT_SCALE}. If given {@code number} is null, then return null
     */
    public static BigDecimal setDefaultScale(BigDecimal number) {
        if (number == null) {
            return null;
        }

        int scale = Math.min(Math.max(number.scale(), 0), MathUtils.DEFAULT_SCALE);
        return number.setScale(scale, RoundingMode.HALF_UP);
    }

    // endregion

    // region comparisons

    /**
     * @return true if {@code value1} equals {@code value2}, or else false
     */
    public static boolean numbersEqual(BigDecimal value1, BigDecimal value2) {
        return value1.compareTo(value2) == 0;
    }

    /**
     * @return true if {@code value1} equals {@code value2}, or else false
     */
    public static boolean numbersEqual(BigDecimal value1, int value2) {
        return numbersEqual(value1, BigDecimal.valueOf(value2));
    }

    /**
     * @return true if {@code value1} equals {@code value2}, or else false
     */
    public static boolean numbersEqual(BigDecimal value1, double value2) {
        return numbersEqual(value1, BigDecimal.valueOf(value2));
    }

    /**
     * @return true if {@code value1} is greater than {@code value2}, or else false
     */
    public static boolean isGreater(BigDecimal value1, long value2) {
        return isGreater(value1, BigDecimal.valueOf(value2));
    }

    /**
     * @return true if {@code value1} is greater than {@code value2}, or else false
     */
    public static boolean isGreater(BigDecimal value1, double value2) {
        return isGreater(value1, BigDecimal.valueOf(value2));
    }

    /**
     * @return true if {@code value1} is greater than {@code value2}, or else false
     */
    public static boolean isGreater(BigDecimal value1, BigDecimal value2) {
        return value1.compareTo(value2) > 0;
    }

    /**
     * @return true if {@code value1} is lower than {@code value2}, or else false
     */
    public static boolean isLower(BigDecimal value1, long value2) {
        return isLower(value1, BigDecimal.valueOf(value2));
    }

    /**
     * @return true if {@code value1} is lower than {@code value2}, or else false
     */
    public static boolean isLower(BigDecimal value1, double value2) {
        return isLower(value1, BigDecimal.valueOf(value2));
    }

    /**
     * @return true if {@code value1} is lower than {@code value2}, or else false
     */
    public static boolean isLower(BigDecimal value1, BigDecimal value2) {
        return value1.compareTo(value2) < 0;
    }

    /**
     * @return maximum of given {@code values}, or null if {@code values} is empty
     */
    @Nullable
    public static Double max(List<Double> values) {
        return values.stream()
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    /**
     * @return minimum of given {@code values}, or null if {@code values} is empty
     */
    @Nullable
    public static Double min(List<Double> values) {
        return values.stream()
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    // endregion

}