package ru.obukhov.investor.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

public class MathUtils {

    public static BigDecimal getAverage(Collection<BigDecimal> numbers) {
        return numbers.stream()
                .reduce(BigDecimal::add)
                .map(s -> divideMoney(s, numbers.size()))
                .orElse(BigDecimal.ZERO);
    }

    public static BigDecimal divideMoney(BigDecimal dividend, int divisor) {
        return dividend.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP);
    }

    public static boolean numbersEqual(BigDecimal value1, int value2) {
        return value1.compareTo(BigDecimal.valueOf(value2)) == 0;
    }

}
