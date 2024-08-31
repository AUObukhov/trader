package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@UtilityClass
public class DecimalUtils {

    /**
     * Scale for operations with money
     */
    public static final int DEFAULT_SCALE = 9;

    /**
     * Some complex operations, such as weighted average, loose accuracy due to rounding of each intermediate result.
     * This constant is intended to reduce this inaccuracy by increasing accuracy of intermediate operations.
     * Must be significantly larger than {@link DecimalUtils#DEFAULT_SCALE}
     */
    private static final int HIGH_SCALE = 15;

    public static final BigDecimal ZERO = setDefaultScale(0);
    public static final BigDecimal ONE = setDefaultScale(1);

    public static BigDecimal newBigDecimal(final long units, final int nano) {
        return units == 0 && nano == 0
                ? BigDecimal.valueOf(0, DecimalUtils.DEFAULT_SCALE)
                : BigDecimal.valueOf(units).add(BigDecimal.valueOf(nano, DecimalUtils.DEFAULT_SCALE));
    }

    public static BigDecimal newBigDecimal(final MoneyValue moneyValue) {
        return moneyValue == null
                ? null
                : newBigDecimal(moneyValue.getUnits(), moneyValue.getNano());
    }

    public static int getNano(@NotNull final BigDecimal bigDecimal) {
        return bigDecimal.remainder(BigDecimal.ONE)
                .movePointRight(DEFAULT_SCALE)
                .intValue();
    }

    public static Quotation toQuotation(@NotNull final BigDecimal bigDecimal) {
        return Quotation.newBuilder()
                .setUnits(bigDecimal.longValue())
                .setNano(getNano(bigDecimal))
                .build();
    }

    public static BigDecimal add(final BigDecimal addend1, final long addend2) {
        return setDefaultScale(addend1.add(BigDecimal.valueOf(addend2)));
    }

    public static BigDecimal subtract(final BigDecimal minuend, final double subtrahend) {
        return setDefaultScale(minuend.subtract(BigDecimal.valueOf(subtrahend)));
    }

    // region multiply overloads

    public static BigDecimal multiply(final BigDecimal multiplier, final BigDecimal multiplicand) {
        return setDefaultScale(multiplier.multiply(multiplicand));
    }

    public static BigDecimal multiply(final BigDecimal multiplier, final double multiplicand) {
        return multiply(multiplier, BigDecimal.valueOf(multiplicand));
    }

    public static BigDecimal multiply(final BigDecimal multiplier, final int multiplicand) {
        return multiply(multiplier, BigDecimal.valueOf(multiplicand));
    }

    // endregion

    // region divide overloads

    public static BigDecimal divide(final BigDecimal dividend, final int divisor) {
        return divide(dividend, BigDecimal.valueOf(divisor));
    }

    public static BigDecimal divide(final BigDecimal dividend, final double divisor) {
        return divide(dividend, BigDecimal.valueOf(divisor));
    }

    public static BigDecimal divide(final double dividend, final double divisor) {
        return divide(BigDecimal.valueOf(dividend), BigDecimal.valueOf(divisor));
    }

    public static BigDecimal divide(final BigDecimal dividend, final BigDecimal divisor) {
        return dividend.divide(divisor, DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal divide(long dividend, BigDecimal divisor) {
        return divide(BigDecimal.valueOf(dividend), divisor);
    }

    public static BigDecimal divideAccurate(final BigDecimal dividend, final BigDecimal divisor) {
        return dividend.divide(divisor, HIGH_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal divideAccurate(long dividend, BigDecimal divisor) {
        return divideAccurate(BigDecimal.valueOf(dividend), divisor);
    }

    // endregion

    public static BigDecimal getAverage(final BigDecimal value1, final BigDecimal value2) {
        return divide(value1.add(value2), 2);
    }

    public static BigDecimal getAverage(final List<BigDecimal> values, final List<Integer> weights) {
        BigDecimal sum = DecimalUtils.ZERO;
        for (int i = 0; i < values.size(); i++) {
            sum = sum.add(DecimalUtils.multiply(values.get(i), weights.get(i)));
        }
        return DecimalUtils.divide(sum, weights.stream().reduce(Integer::sum).orElseThrow());
    }

    public static BigDecimal addFraction(final BigDecimal number, final BigDecimal fraction) {
        return setDefaultScale(number.multiply(BigDecimal.valueOf(1).add(fraction)));
    }

    public static BigDecimal subtractFraction(final BigDecimal number, final BigDecimal fraction) {
        return setDefaultScale(number.multiply(BigDecimal.valueOf(1).subtract(fraction)));
    }

    public static BigDecimal getFractionDifference(final BigDecimal number1, final BigDecimal number2) {
        return divide(number1, number2).subtract(BigDecimal.ONE);
    }

    // region setDefaultScale overloads

    public static BigDecimal setDefaultScale(final BigDecimal number) {
        if (number == null) {
            return null;
        }

        return number.setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal setDefaultScale(final Double number) {
        if (number == null) {
            return null;
        }

        return BigDecimal.valueOf(number).setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal setDefaultScale(final Long number) {
        if (number == null) {
            return null;
        }

        return BigDecimal.valueOf(number).setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal setDefaultScale(final Integer number) {
        if (number == null) {
            return null;
        }

        return BigDecimal.valueOf(number).setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    // endregion

    // region numbersEqual overloads

    public static boolean numbersEqual(@Nullable final BigDecimal value1, @Nullable final BigDecimal value2) {
        return value1 == null
                ? value2 == null
                : value2 != null && value1.compareTo(value2) == 0;
    }

    public static boolean numbersEqual(final BigDecimal value1, final int value2) {
        return numbersEqual(value1, BigDecimal.valueOf(value2));
    }

    public static boolean numbersEqual(final BigDecimal value1, final double value2) {
        return numbersEqual(value1, BigDecimal.valueOf(value2));
    }

    // endregion

    public static BigDecimal stripTrailingZerosSafe(@Nullable final BigDecimal value) {
        return value == null ? null : value.stripTrailingZeros();
    }

    public static String toPrettyStringSafe(@Nullable final BigDecimal value) {
        return value == null ? null : value.stripTrailingZeros().toPlainString();
    }

}