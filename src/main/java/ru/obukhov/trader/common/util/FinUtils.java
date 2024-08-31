package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class FinUtils {

    public static double getRelativeProfit(final BigDecimal investment, final BigDecimal absoluteProfit) {
        final int investmentSign = investment.signum();
        Assert.isTrue(investmentSign >= 0, "investment can't be negative");

        return investmentSign == 0
                ? 0.0
                : absoluteProfit.divide(investment, RoundingMode.HALF_UP).doubleValue();
    }

    public static double getAverageAnnualReturn(final double daysCount, final double relativeProfit) {
        Assert.isTrue(daysCount >= 1, "daysCount can't be lower than 1");
        Assert.isTrue(relativeProfit >= -1.0, "relativeProfit can't be lower than -1");

        final double pow = Math.pow(relativeProfit + 1, DateUtils.DAYS_IN_YEAR / daysCount);
        // using BigDecimal due to Double's loss of precision, but it is not necessary
        return DecimalUtils.subtract(DecimalUtils.setDefaultScale(pow), 1).doubleValue();
    }

}