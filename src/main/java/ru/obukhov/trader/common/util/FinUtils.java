package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilities for financial calculations
 */
@UtilityClass
public class FinUtils {

    /**
     * @param investment     invested amount
     * @param absoluteProfit absolute absoluteProfit amount
     * @return result of expression [{@code absoluteProfit / investment}].
     * If investment = 0, then returns 0
     */
    public static double getRelativeProfit(final BigDecimal investment, final BigDecimal absoluteProfit) {
        final int investmentSign = investment.signum();
        Assert.isTrue(investmentSign >= 0, "investment can't be negative");

        return investmentSign == 0
                ? 0.0
                : absoluteProfit.divide(investment, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Calculates average annual return by compound interest formula.
     *
     * @param daysCount      interval size in days. Must be greater than 0.
     * @param relativeProfit relative profit gained in {@code daysCount} days. Min value is -1.
     * @return result of expression {@code (relativeProfit + 1) / (}{@link DateUtils#DAYS_IN_YEAR} {@code / daysCount)}
     */
    public static double getAverageAnnualReturn(final double daysCount, final double relativeProfit) {
        Assert.isTrue(daysCount >= 1, "daysCount can't be lower than 1");
        Assert.isTrue(relativeProfit >= -1.0, "relativeProfit can't be lower than -1");

        final double pow = Math.pow(relativeProfit + 1, DateUtils.DAYS_IN_YEAR / daysCount);
        // using BigDecimal due to Double's loss of precision, but it is not necessary
        return DecimalUtils.subtract(BigDecimal.valueOf(pow), 1).doubleValue();
    }

}