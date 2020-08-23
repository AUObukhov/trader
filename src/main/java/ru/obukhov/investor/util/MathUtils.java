package ru.obukhov.investor.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MathUtils {

    public static BigDecimal getAverageMoney(Collection<BigDecimal> numbers) {
        return numbers.stream()
                .reduce(BigDecimal::add)
                .map(s -> divideMoney(s, numbers.size()))
                .orElse(BigDecimal.ZERO);
    }

    public static BigDecimal divideMoney(BigDecimal dividend, int divisor) {
        return dividend.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP);
    }

    public static boolean numbersEqual(BigDecimal value1, BigDecimal value2) {
        return value1.compareTo(value2) == 0;
    }

    public static boolean numbersEqual(BigDecimal value1, int value2) {
        return numbersEqual(value1, BigDecimal.valueOf(value2));
    }

    public static boolean numbersEqual(BigDecimal value1, double value2) {
        return numbersEqual(value1, BigDecimal.valueOf(value2));
    }

}