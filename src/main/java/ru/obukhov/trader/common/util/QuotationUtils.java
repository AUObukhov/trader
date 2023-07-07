package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

@UtilityClass
public class QuotationUtils {

    public static final int NANOS_LIMIT = 1_000_000_000;

    public static Quotation newNormalizedQuotation(final long units, final int nano) {
        // normalization
        if (units < 0 && nano > 0) {
            return Quotation.newBuilder()
                    .setUnits(units + 1)
                    .setNano(nano - NANOS_LIMIT)
                    .build();
        } else if (units > 0 && nano < 0) {
            return Quotation.newBuilder()
                    .setUnits(units - 1)
                    .setNano(nano + NANOS_LIMIT)
                    .build();
        }
        return Quotation.newBuilder()
                .setUnits(units)
                .setNano(nano)
                .build();
    }

    private static Quotation newQuotationHandlingAddingOverflow(long units, int nano) {
        // overflow handling
        if (nano >= NANOS_LIMIT) {
            units = Math.incrementExact(units);
            nano -= NANOS_LIMIT;
        } else if (nano <= -NANOS_LIMIT) {
            units = Math.decrementExact(units);
            nano += NANOS_LIMIT;
        }

        return newNormalizedQuotation(units, nano);
    }

    private static Quotation newQuotationHandlingMultiplyingOverflow(long units, long nano) {
        // overflow handling
        if (nano >= NANOS_LIMIT || nano <= -NANOS_LIMIT) {
            units = Math.addExact(units, nano / NANOS_LIMIT);
            nano %= NANOS_LIMIT;
        }

        return newNormalizedQuotation(units, (int) nano);
    }

    public static String toString(final Quotation quotation) {
        return "[" + quotation.getUnits() + "; " + quotation.getNano() + "]";
    }

    public static String toPrettyString(final Quotation quotation) {
        if (quotation.getNano() == 0) {
            return String.valueOf(quotation.getUnits());
        } else {
            final int sign = getSign(quotation);
            String fractionString = String.valueOf(Math.abs(quotation.getNano()));
            fractionString = StringUtils.leftPad(fractionString, 9, '0');
            fractionString = fractionString.replaceAll("0*$", "");
            return (sign >= 0 ? "" : "-") + Math.absExact(quotation.getUnits()) + '.' + fractionString;
        }
    }

    public static int getSign(final Quotation quotation) {
        return Long.signum(quotation.getUnits() | quotation.getNano());
    }

    public static int compare(final Quotation left, final Quotation right) {
        final long diffUnits = left.getUnits() - right.getUnits();
        final int diffNano = left.getNano() - right.getNano();
        return Long.signum(diffUnits | diffNano);
    }

    // region add

    public static Quotation add(final Quotation term1, final Quotation term2) {
        long units = Math.addExact(term1.getUnits(), term2.getUnits());
        int nano = term1.getNano() + term2.getNano();

        return newQuotationHandlingAddingOverflow(units, nano);
    }

    public static Quotation add(final Quotation term1, final double term2) {
        final long longTerm2 = (long) term2;
        long units = Math.addExact(term1.getUnits(), longTerm2);
        int nano = term1.getNano() + (int) Math.round(((term2 - longTerm2) * NANOS_LIMIT));

        return newQuotationHandlingAddingOverflow(units, nano);
    }

    public static Quotation add(final Quotation term1, final long term2) {
        final long units = Math.addExact(term1.getUnits(), term2);
        return newNormalizedQuotation(units, term1.getNano());
    }

    // endregion

    // region subtract

    public static Quotation subtract(final Quotation minuend, final Quotation subtrahend) {
        long units = Math.subtractExact(minuend.getUnits(), subtrahend.getUnits());
        int nano = minuend.getNano() - subtrahend.getNano();

        return newQuotationHandlingAddingOverflow(units, nano);
    }

    public static Quotation subtract(final Quotation minuend, final double subtrahend) {
        final long longSubtrahend = (long) subtrahend;
        long units = Math.subtractExact(minuend.getUnits(), longSubtrahend);
        int nano = minuend.getNano() - (int) Math.round((subtrahend - longSubtrahend) * NANOS_LIMIT);

        return newQuotationHandlingAddingOverflow(units, nano);
    }

    public static Quotation subtract(final Quotation minuend, final long subtrahend) {
        final long units = Math.subtractExact(minuend.getUnits(), subtrahend);
        return newNormalizedQuotation(units, minuend.getNano());
    }

    // endregion

    // region multiply

    public static Quotation multiply(final Quotation multiplicand1, final Quotation multiplicand2) {
        long units = Math.multiplyExact(multiplicand1.getUnits(), multiplicand2.getUnits());
        final long nanosProduct = ((long) multiplicand1.getNano()) * multiplicand2.getNano();
        final long term1 = Math.multiplyExact(multiplicand1.getUnits(), multiplicand2.getNano());
        final long term2 = Math.multiplyExact(multiplicand1.getNano(), multiplicand2.getUnits());
        final long term3 = MathUtils.divideRoundUp(nanosProduct, NANOS_LIMIT);
        long nano = Math.addExact(Math.addExact(term1, term2), term3);

        return newQuotationHandlingMultiplyingOverflow(units, nano);
    }

    public static Quotation multiply(final Quotation multiplicand1, final double multiplicand2) {
        final long longMultiplicand2 = (long) multiplicand2;
        final Quotation multiplicand2Quotation = Quotation.newBuilder()
                .setUnits(longMultiplicand2)
                .setNano((int) ((multiplicand2 - longMultiplicand2) * NANOS_LIMIT))
                .build();

        return multiply(multiplicand1, multiplicand2Quotation);
    }

    public static Quotation multiply(final Quotation multiplicand1, final long multiplicand2) {
        long units = Math.multiplyExact(multiplicand1.getUnits(), multiplicand2);
        long nano = Math.multiplyExact(multiplicand1.getNano(), multiplicand2);

        return newQuotationHandlingMultiplyingOverflow(units, nano);
    }

    // endregion

    // region divide

    public static Quotation divide(final Quotation dividend, final Quotation divisor) {
        validateDivisorIsNotZero(divisor);

        final int sign = getSign(dividend) * getSign(divisor);

        final long dividendUnitsAbs = Math.absExact(dividend.getUnits());
        final int dividendNanoAbs = Math.abs(dividend.getNano());

        final long divisorUnitsAbs = Math.absExact(divisor.getUnits());
        final int divisorNanoAbs = Math.abs(divisor.getNano());

        final Int128 dividendAdjusted = Int128.multiplyPositive(dividendUnitsAbs, NANOS_LIMIT);
        dividendAdjusted.selfAddExact(dividendNanoAbs);
        final Int128 divisorAdjusted = Int128.multiplyPositive(divisorUnitsAbs, NANOS_LIMIT);
        divisorAdjusted.selfAddExact(divisorNanoAbs);

        final Int128 quotient = dividendAdjusted.selfDividePositive(divisorAdjusted);

        final long units = Math.multiplyExact(sign, quotient.toLongExact());
        final int nano = Math.multiplyExact(sign, getNanoForDivision(dividendAdjusted, divisorAdjusted));

        return newNormalizedQuotation(units, nano);
    }

    public static Quotation divide(final Quotation dividend, final double divisor) {
        final long longDivisor = (long) divisor;
        final Quotation divisorQuotation = Quotation.newBuilder()
                .setUnits(longDivisor)
                .setNano((int) ((divisor - longDivisor) * NANOS_LIMIT))
                .build();

        return divide(dividend, divisorQuotation);
    }

    public static Quotation divide(final Quotation dividend, final long divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Division by zero");
        }

        final int sign = getSign(dividend) * Long.signum(divisor);

        final long dividendUnitsAbs = Math.absExact(dividend.getUnits());
        final int dividendNanoAbs = Math.absExact(dividend.getNano());

        final long divisorUnitsAbs = Math.abs(divisor);

        final long dividendAbsAdjusted = Math.addExact(Math.multiplyExact(dividendUnitsAbs, NANOS_LIMIT), dividendNanoAbs);
        final long divisorAbsAdjusted = Math.multiplyExact(divisorUnitsAbs, NANOS_LIMIT);

        final long quotient = dividendAbsAdjusted / divisorAbsAdjusted;
        final long remainder = dividendAbsAdjusted % divisorAbsAdjusted;

        final long units = Math.multiplyExact(sign, quotient);

        final int nano = Math.multiplyExact(sign, getNanoForDivision(remainder, divisorAbsAdjusted));
        return newNormalizedQuotation(units, nano);
    }

    public static Quotation divide(final long dividend, final Quotation divisor) {
        validateDivisorIsNotZero(divisor);

        final int sign = Long.signum(dividend) * getSign(divisor);

        final long dividendAbs = Math.absExact(dividend);

        final long divisorUnitsAbs = Math.absExact(divisor.getUnits());
        final int divisorNanoAbs = Math.absExact(divisor.getNano());

        final long dividendAbsAdjusted = Math.multiplyExact(dividendAbs, NANOS_LIMIT);
        final long divisorAbsAdjusted = Math.addExact(Math.multiplyExact(divisorUnitsAbs, NANOS_LIMIT), divisorNanoAbs);

        final long quotient = dividendAbsAdjusted / divisorAbsAdjusted;
        final long remainder = dividendAbsAdjusted % divisorAbsAdjusted;

        final long units = Math.multiplyExact(sign, quotient);

        final int nano = Math.multiplyExact(sign, getNanoForDivision(remainder, divisorAbsAdjusted));
        return newNormalizedQuotation(units, nano);
    }

    private static int getNanoForDivision(final long remainder, final long divisorAdjusted) {
        final Int128 int128RemainderAdjusted = Int128.multiplyPositive(remainder, NANOS_LIMIT);

        final Int128 quotient = int128RemainderAdjusted.selfDividePositive(divisorAdjusted);
        final long secondRemainder = int128RemainderAdjusted.toLongExact();
        final int intQuotient = quotient.toIntExact();
        return Math.multiplyExact(2, secondRemainder) >= divisorAdjusted
                ? Math.incrementExact(intQuotient)
                : intQuotient;
    }

    private static int getNanoForDivision(final Int128 remainder, final Int128 divisorAdjusted) {
        final Int128 int128RemainderAdjusted = remainder.multiplyExact(NANOS_LIMIT);

        final Int128 quotient = int128RemainderAdjusted.selfDividePositive(divisorAdjusted);
        final int intQuotient = quotient.toIntExact();
        return int128RemainderAdjusted.multiplyExact(2).compare(divisorAdjusted) >= 0
                ? Math.incrementExact(intQuotient)
                : intQuotient;
    }

    private static void validateDivisorIsNotZero(final Quotation divisor) {
        if (divisor.getUnits() == 0 && divisor.getNano() == 0) {
            throw new ArithmeticException("Division by zero");
        }
    }

    // endregion

}