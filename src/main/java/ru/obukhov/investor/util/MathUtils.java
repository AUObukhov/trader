package ru.obukhov.investor.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MathUtils {

    /**
     * @return average value of passed collection with scale = 2 and rounding mode = Half Up
     */
    public static BigDecimal getAverageMoney(Collection<BigDecimal> numbers) {
        return numbers.stream()
                .reduce(BigDecimal::add)
                .map(s -> divideMoney(s, numbers.size()))
                .orElse(BigDecimal.ZERO);
    }

    /**
     * @return resualt of division if  {@code dividend} by {@code divisor} with scale = 2 and rounding mode = Half Up
     */
    public static BigDecimal divideMoney(BigDecimal dividend, int divisor) {
        return dividend.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP);
    }

    /**
     * @return true if @code value1} equals {@code value2}, or else false
     */
    public static boolean numbersEqual(BigDecimal value1, BigDecimal value2) {
        return value1.compareTo(value2) == 0;
    }

    /**
     * @return true if @code value1} equals {@code value2}, or else false
     */
    public static boolean numbersEqual(BigDecimal value1, int value2) {
        return numbersEqual(value1, BigDecimal.valueOf(value2));
    }

    /**
     * @return true if @code value1} equals {@code value2}, or else false
     */
    public static boolean numbersEqual(BigDecimal value1, double value2) {
        return numbersEqual(value1, BigDecimal.valueOf(value2));
    }

}