package ru.obukhov.trader.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

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
     * Calculates weighted average of given amounts.<br/>
     * Weight is count of milliseconds every amount was actual.
     * Every amount is actual from its begin dateTime inclusive to begin dateTime of next amount exclusive.
     * The last amount is actual until given {@code endDateTime}
     *
     * @param dateTimesToAmounts begin dateTimes and corresponding amounts
     * @param endDateTime        end dateTime of last amount from given {@code dateTimesToAmounts}
     * @return weighted average or zero if given {@code dateTimesToAmounts} is empty
     */
    public static BigDecimal getWeightedAverage(SortedMap<OffsetDateTime, BigDecimal> dateTimesToAmounts,
                                                OffsetDateTime endDateTime) {
        if (dateTimesToAmounts.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Map<BigDecimal, Double> weightedAmounts = getWeightedAmounts(dateTimesToAmounts, endDateTime);

        return getWeightedAverage(weightedAmounts);
    }

    private static Map<BigDecimal, Double> getWeightedAmounts(SortedMap<OffsetDateTime, BigDecimal> dateTimesToAmounts,
                                                              OffsetDateTime endDateTime) {

        Map<BigDecimal, Double> weightedInvestments = new HashMap<>();
        Iterator<Map.Entry<OffsetDateTime, BigDecimal>> iterator = dateTimesToAmounts.entrySet().iterator();
        Map.Entry<OffsetDateTime, BigDecimal> currentEntry = iterator.next();
        while (iterator.hasNext()) {
            Map.Entry<OffsetDateTime, BigDecimal> nextEntry = iterator.next();
            addWeightedAmount(weightedInvestments, currentEntry.getValue(), currentEntry.getKey(), nextEntry.getKey());

            currentEntry = nextEntry;
        }
        addWeightedAmount(weightedInvestments, currentEntry.getValue(), currentEntry.getKey(), endDateTime);

        return weightedInvestments;
    }

    private static void addWeightedAmount(Map<BigDecimal, Double> weightedAmounts,
                                          BigDecimal amount,
                                          OffsetDateTime from,
                                          OffsetDateTime to) {
        Double weight = (double) Duration.between(from, to).toMillis();
        weightedAmounts.put(amount, weight);
    }

    private static BigDecimal getWeightedAverage(Map<BigDecimal, Double> weightedAmounts) {
        double weightsSum = weightedAmounts.values().stream().reduce(0.0, Double::sum);

        BigDecimal weightedAverage = BigDecimal.ZERO;
        for (Map.Entry<BigDecimal, Double> entry : weightedAmounts.entrySet()) {
            double weight = entry.getValue() / weightsSum;
            BigDecimal weightedAmount = MathUtils.multiply(entry.getKey(), weight);
            weightedAverage = weightedAverage.add(weightedAmount);
        }
        return MathUtils.setDefaultScale(weightedAverage);
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
     * @return result of division of {@code dividend} by {@code divisor} with scale = {@link MathUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
     */
    public static BigDecimal divide(BigDecimal dividend, int divisor) {
        return divide(dividend, BigDecimal.valueOf(divisor));
    }

    /**
     * @return result of division of {@code dividend} by {@code divisor} with scale = {@link MathUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
     */
    public static BigDecimal divide(BigDecimal dividend, double divisor) {
        return divide(dividend, BigDecimal.valueOf(divisor));
    }

    /**
     * @return result of division of {@code dividend} by {@code divisor} with scale = {@link MathUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
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