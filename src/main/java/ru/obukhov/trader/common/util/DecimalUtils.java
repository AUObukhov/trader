package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class DecimalUtils {

    public static final int DEFAULT_SCALE = 5;

    /**
     * @return minuend - subtrahend
     */
    public static BigDecimal subtract(final BigDecimal minuend, final double subtrahend) {
        return setDefaultScale(minuend.subtract(BigDecimal.valueOf(subtrahend)));
    }

    // region multiply overloads

    /**
     * @return multiplier * multiplicand with scale = {@link DecimalUtils#DEFAULT_SCALE}
     */
    public static BigDecimal multiply(final BigDecimal multiplier, final double multiplicand) {
        return setDefaultScale(multiplier.multiply(BigDecimal.valueOf(multiplicand)));
    }

    /**
     * @return multiplier * multiplicand with scale = {@link DecimalUtils#DEFAULT_SCALE}
     */
    public static BigDecimal multiply(final BigDecimal multiplier, final int multiplicand) {
        return setDefaultScale(multiplier.multiply(BigDecimal.valueOf(multiplicand)));
    }

    // endregion

    // region divide overloads

    /**
     * @return result of division of {@code dividend} by {@code divisor} with scale = {@link DecimalUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
     */
    public static BigDecimal divide(final BigDecimal dividend, final int divisor) {
        return divide(dividend, BigDecimal.valueOf(divisor));
    }

    /**
     * @return result of division of {@code dividend} by {@code divisor} with scale = {@link DecimalUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
     */
    public static BigDecimal divide(final BigDecimal dividend, final double divisor) {
        return divide(dividend, BigDecimal.valueOf(divisor));
    }

    /**
     * @return result of division of {@code dividend} by {@code divisor} with scale = {@link DecimalUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
     */
    public static BigDecimal divide(final double dividend, final double divisor) {
        return divide(BigDecimal.valueOf(dividend), BigDecimal.valueOf(divisor));
    }

    /**
     * @return result of division of {@code dividend} by {@code divisor} with scale = {@link DecimalUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
     */
    public static BigDecimal divide(final BigDecimal dividend, final BigDecimal divisor) {
        return dividend.divide(divisor, DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal divide(long dividend, BigDecimal divisor) {
        return divide(BigDecimal.valueOf(dividend), divisor);
    }

    // endregion

    /**
     * @return average between given {@code value1} and {@code value2} with scale = {@link DecimalUtils#DEFAULT_SCALE}
     */
    public static BigDecimal getAverage(final BigDecimal value1, final BigDecimal value2) {
        return divide(value1.add(value2), 2);
    }

    /**
     * @return integer quotient of division of {@code dividend} by {@code divisor}
     */
    public static int getIntegerQuotient(final BigDecimal dividend, final BigDecimal divisor) {
        return dividend.divide(divisor, RoundingMode.DOWN).intValue();
    }

    /**
     * @return {@code number} * {@code fraction}
     */
    public static BigDecimal getFraction(final BigDecimal number, final double fraction) {
        return multiply(number, fraction);
    }

    /**
     * @return {@code number} * (1 + {@code fraction})
     */
    public static BigDecimal addFraction(final BigDecimal number, final double fraction) {
        return multiply(number, 1 + fraction);
    }

    /**
     * @return {@code number} * (1 - {@code fraction})
     */
    public static BigDecimal subtractFraction(final BigDecimal number, final double fraction) {
        return multiply(number, 1 - fraction);
    }

    /**
     * @return {@code number1} / {@code number2} - 1
     */
    public static BigDecimal getFractionDifference(final BigDecimal number1, final BigDecimal number2) {
        return divide(number1, number2).subtract(BigDecimal.ONE);
    }

    // region setDefaultScale overloads

    /**
     * @return BigDecimal equals to give {@code number} with scale {@link DecimalUtils#DEFAULT_SCALE}.
     * If given {@code number} is null, then returns null
     */
    public static BigDecimal setDefaultScale(final BigDecimal number) {
        if (number == null) {
            return null;
        }

        return number.setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * @return BigDecimal equals to give {@code number} with scale {@link DecimalUtils#DEFAULT_SCALE}.
     * If given {@code number} is null, then returns null
     */
    public static BigDecimal setDefaultScale(final Double number) {
        if (number == null) {
            return null;
        }

        return BigDecimal.valueOf(number).setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * @return BigDecimal equals to give {@code number} with scale {@link DecimalUtils#DEFAULT_SCALE}.
     * If given {@code number} is null, then returns null
     */
    public static BigDecimal setDefaultScale(final Long number) {
        if (number == null) {
            return null;
        }

        return BigDecimal.valueOf(number).setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * @return BigDecimal equals to give {@code number} with scale {@link DecimalUtils#DEFAULT_SCALE}.
     * If given {@code number} is null, then returns null
     */
    public static BigDecimal setDefaultScale(final Integer number) {
        if (number == null) {
            return null;
        }

        return BigDecimal.valueOf(number).setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    // endregion

    // region numbersEqual overloads

    /**
     * @return true if {@code value1} equals {@code value2}, or else false
     */
    public static boolean numbersEqual(@Nullable final BigDecimal value1, @Nullable final BigDecimal value2) {
        return value1 == null
                ? value2 == null
                : value2 != null && value1.compareTo(value2) == 0;
    }

    /**
     * @return true if {@code value1} equals {@code value2}, or else false
     */
    public static boolean numbersEqual(final BigDecimal value1, final int value2) {
        return numbersEqual(value1, BigDecimal.valueOf(value2));
    }

    /**
     * @return true if {@code value1} equals {@code value2}, or else false
     */
    public static boolean numbersEqual(final BigDecimal value1, final double value2) {
        return numbersEqual(value1, BigDecimal.valueOf(value2));
    }

    // endregion

}