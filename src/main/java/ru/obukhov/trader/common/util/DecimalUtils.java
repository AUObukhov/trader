package ru.obukhov.trader.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DecimalUtils {

    public static final int DEFAULT_SCALE = 5;

    /**
     * @return minuend - subtrahend
     */
    public static BigDecimal subtract(BigDecimal minuend, double subtrahend) {
        return minuend.subtract(BigDecimal.valueOf(subtrahend));
    }

    // region multiply overloads

    /**
     * @return multiplier * multiplicand
     */
    public static BigDecimal multiply(BigDecimal multiplier, double multiplicand) {
        return multiplier.multiply(BigDecimal.valueOf(multiplicand));
    }

    /**
     * @return multiplier * multiplicand
     */
    public static BigDecimal multiply(BigDecimal multiplier, int multiplicand) {
        return multiplier.multiply(BigDecimal.valueOf(multiplicand));
    }

    // endregion

    // region divide overloads

    /**
     * @return result of division of {@code dividend} by {@code divisor} with scale = {@link DecimalUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
     */
    public static BigDecimal divide(BigDecimal dividend, int divisor) {
        return divide(dividend, BigDecimal.valueOf(divisor));
    }

    /**
     * @return result of division of {@code dividend} by {@code divisor} with scale = {@link DecimalUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
     */
    public static BigDecimal divide(BigDecimal dividend, double divisor) {
        return divide(dividend, BigDecimal.valueOf(divisor));
    }

    /**
     * @return result of division of {@code dividend} by {@code divisor} with scale = {@link DecimalUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
     */
    public static BigDecimal divide(double dividend, double divisor) {
        return divide(BigDecimal.valueOf(dividend), BigDecimal.valueOf(divisor));
    }

    /**
     * @return result of division of {@code dividend} by {@code divisor} with scale = {@link DecimalUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        return dividend.divide(divisor, DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    // endregion

    /**
     * @return average between given {@code value1} and {@code value2} with scale = {@link DecimalUtils#DEFAULT_SCALE}
     */
    public static BigDecimal getAverage(BigDecimal value1, BigDecimal value2) {
        return divide(value1.add(value2), 2);
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

    // region setDefaultScale overloads

    /**
     * @return BigDecimal with scale equal to minimum non negative value between scale of given {@code number}, 0 and
     * {@link DecimalUtils#DEFAULT_SCALE}. If given {@code number} is null, then return null
     */
    public static BigDecimal setDefaultScale(BigDecimal number) {
        if (number == null) {
            return null;
        }

        int scale = Math.min(Math.max(number.scale(), 0), DEFAULT_SCALE);
        return number.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * @return BigDecimal with scale equal to minimum non negative value between scale of given {@code number}, 0 and
     * {@link DecimalUtils#DEFAULT_SCALE}. If given {@code number} is null, then return null
     */
    public static BigDecimal setDefaultScale(Double number) {
        if (number == null) {
            return null;
        }

        BigDecimal bigDecimal = BigDecimal.valueOf(number);

        int scale = Math.min(Math.max(bigDecimal.scale(), 0), DEFAULT_SCALE);
        return bigDecimal.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * @return BigDecimal with scale zero. If given {@code number} is null, then return null
     */
    public static BigDecimal setDefaultScale(Long number) {
        if (number == null) {
            return null;
        }

        BigDecimal bigDecimal = BigDecimal.valueOf(number);

        int scale = Math.min(Math.max(bigDecimal.scale(), 0), DEFAULT_SCALE);
        return bigDecimal.setScale(scale, RoundingMode.HALF_UP);
    }

    // endregion

    // region numbersEqual overloads

    /**
     * @return true if {@code value1} equals {@code value2}, or else false
     */
    public static boolean numbersEqual(@Nullable BigDecimal value1, @Nullable BigDecimal value2) {
        return value1 == null
                ? value2 == null
                : value2 != null && value1.compareTo(value2) == 0;
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

    // endregion

    // region isGreater overloads

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

    // endregion

    // region isLower overloads

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

    // endregion

}