package ru.obukhov.trader.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Int128 {

    private long low;
    private long high;

    public Int128(final long low) {
        this(low, low >> 63);
    }

    // region conversion methods

    static boolean isLong(final long low, final long high) {
        return high == (low >> 63);
    }

    boolean isLong() {
        return high == (low >> 63);
    }

    boolean isInt() {
        return high == (low >> 31);
    }

    @Override
    public String toString() {
        return "(low = " + low + ", high = " + high + ")";
    }

    public long toLongExact() {
        if (!isLong()) {
            throw new ArithmeticException("value too big for a long: " + this);
        }

        return low;
    }

    public int toIntExact() {
        if (!isInt()) {
            throw new ArithmeticException("value too big for a int: " + this);
        }

        return (int) low;
    }

    // endregion

    // region comparison methods

    public int compare(final Int128 other) {
        int result = Long.compare(high, other.high);

        if (result == 0) {
            result = Long.compareUnsigned(low, other.low);
        }

        return result;
    }

    public int compare(final long other) {
        int result = Long.compare(high, other >> 63);

        if (result == 0) {
            result = Long.compareUnsigned(low, other);
        }

        return result;
    }

    public boolean isZero() {
        return (low | high) == 0;
    }

    // endregion

    // region arithmetic methods

    public void selfIncrementExact() {
        if (low == -1) {
            high = Math.incrementExact(high);
        }
        low++;
    }

    public void selfDecrementExact() {
        if (low == 0) {
            high = Math.decrementExact(high);
        }
        low--;
    }

    // region add methods

    long addHighExact(final long low, final long high) {
        final long result = this.high + high + unsignedCarry(this.low, low);

        // Overflow if both arguments have the opposite sign of the result
        if (((result ^ this.high) & (result ^ high)) < 0) {
            throw new ArithmeticException("Int128 overflow");
        }

        return result;
    }

    public Int128 selfAddExact(final int term) {
        final long termHigh = term >> 31;
        this.high = addHighExact(term, termHigh);
        this.low = low + term;
        return this;
    }

    // endregion

    // region subtract methods

    public void selfSubtract(final Int128 subtrahend) {
        high = high - subtrahend.high - unsignedBorrow(low, subtrahend.low);
        low -= subtrahend.low;
    }

    // endregion

    // region multiply methods

    public Int128 multiplyExact(final int multiplier) {
        final long multiplierHigh = multiplier >> 31;

        final long z1Low = low * multiplier;
        final long z1High = unsignedMultiplyHigh(low, multiplier);

        final long z2Low = low * multiplierHigh;
        final long z2High = unsignedMultiplyHigh(low, multiplierHigh);

        final long z3Low = high * multiplier;
        final long z3High = unsignedMultiplyHigh(high, multiplier);

        final long resultHigh = z1High + z2Low + z3Low;

        if (productOverflows(multiplier, multiplierHigh, z1High, z2Low, z2High, z3Low, z3High, resultHigh)) {
            throw new ArithmeticException("overflow");
        }

        return new Int128(z1Low, resultHigh);
    }

    private boolean productOverflows(
            final long multiplierLow, final long multiplierHigh,
            final long z1High,
            final long z2Low, final long z2High,
            final long z3Low, final long z3High,
            final long resultHigh
    ) {
        final boolean thisIsLong = isLong();
        final boolean multiplierIsLong = isLong(multiplierLow, multiplierHigh);

        if (thisIsLong && multiplierIsLong) {
            return false;
        }

        if (!thisIsLong && !multiplierIsLong) {
            return high == multiplierHigh && resultHigh <= 0
                    || high != multiplierHigh && resultHigh >= 0
                    || high != 0 && high != -1
                    || multiplierHigh != 0 && multiplierHigh != -1;
        }

        // If this fits in a long, z3High is effectively "0", so we only care about z2 for
        // checking whether z2 + z3 + z1High overflows.
        // Similarly, if divisor fits in a long, we only care about z3.
        long wHigh;
        long wLow;
        if (thisIsLong) {
            wHigh = z2High - ifNegative(multiplierHigh, low) - ifNegative(low, multiplierHigh + unsignedBorrow(z2Low, multiplierLow));
            wLow = z2Low - ifNegative(low, multiplierLow);
        } else {
            wHigh = z3High - ifNegative(high, multiplierLow) - ifNegative(multiplierLow, high + unsignedBorrow(z3Low, low));
            wLow = z3Low - ifNegative(multiplierLow, low);
        }

        // t = w + z1High
        final long tLow = wLow + z1High;
        final long tHigh = wHigh + unsignedCarry(wLow, z1High);

        return !isLong(tLow, tHigh);
    }

    /**
     * @return {@code multiplier1 * multiplier2} in Int128 format.
     * The result is unreliable if {@code multiplier1} or {@code multiplier2} is negative
     */
    public static Int128 multiplyPositive(final long multiplier1, final int multiplier2) {
        return new Int128(
                multiplier1 * multiplier2, // overflow goes to high
                unsignedMultiplyHigh(multiplier1, multiplier2)
        );
    }

    public Int128 multiplyPositive(final Int128 multiplier) {
        return new Int128(
                low * multiplier.low,
                unsignedMultiplyHigh(low, multiplier.low) + low * multiplier.high + high * multiplier.low
        );
    }

    // endregion

    // region divide methods

    public Int128 selfDividePositive(final long divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Divide by zero");
        }

        if (compare(divisor) < 0) {
            return new Int128(0, 0);
        }

        if (high == 0) { // this and divisor fit in an unsigned
            final Int128 quotient = new Int128(Long.divideUnsigned(low, divisor), 0);
            low = Long.remainderUnsigned(low, divisor);
            high = 0;
            return quotient;
        }

        if (Long.compareUnsigned(high, divisor) < 0) {
            return divide128by64WithRemainder(divisor);
        }

        final long quotientHigh = Long.divideUnsigned(high, divisor);
        high = Long.remainderUnsigned(high, divisor);
        final Int128 quotient = divide128by64WithRemainder(divisor);
        quotient.setHigh(quotientHigh);
        return quotient;
    }

    /**
     * Divides this Int128 by given {@code divisor}. Replaces this Int128 value by remainder.<br/>
     * This Int128 and {@code divisor} must be positive, otherwise correct result is not guaranteed.
     *
     * @return quotient
     */
    public Int128 selfDividePositive(final Int128 divisor) {
        final int divisorLeadingZeros = divisor.getNumberOfLeadingZeros();
        if (divisorLeadingZeros == 128) {
            throw new ArithmeticException("Divide by zero");
        }

        if (compare(divisor) < 0) {
            return new Int128(0, 0);
        }

        if ((high | divisor.high) == 0) { // this and divisor fit in an unsigned
            final Int128 quotient = new Int128(Long.divideUnsigned(low, divisor.low), 0);
            low = Long.remainderUnsigned(low, divisor.low);
            high = 0;
            return quotient;
        }

        if (divisor.high == 0) {
            if (Long.compareUnsigned(high, divisor.low) < 0) {
                return divide128by64WithRemainder(divisor.low);
            } else {
                final long quotientHigh = Long.divideUnsigned(high, divisor.low);
                high = Long.remainderUnsigned(high, divisor.low);
                final Int128 quotient = divide128by64WithRemainder(divisor.low);
                quotient.setHigh(quotientHigh);
                return quotient;
            }
        }

        final long v1High = Int128.shiftLeftHigh(divisor, divisorLeadingZeros); // v1 = divisor << divisorLeadingZeros

        final Int128 u1 = shiftRightUnsigned(1);

        final Int128 quotient = divide128by64(u1.low, u1.high, v1High);
        quotient.selfShiftRightUnsigned(63 - divisorLeadingZeros);
        if (!quotient.isZero()) {
            quotient.selfDecrementExact();
        }

        selfSubtract(quotient.multiplyPositive(divisor));
        if (compare(divisor) >= 0) {
            quotient.selfIncrementExact();
            selfSubtract(divisor);
        }

        return quotient;
    }

    int getNumberOfLeadingZeros() {
        int count = Long.numberOfLeadingZeros(high);
        if (count == 64) {
            count += Long.numberOfLeadingZeros(low);
        }

        return count;
    }

    private Int128 divide128by64WithRemainder(long divisor) {
        final int shift = Long.numberOfLeadingZeros(divisor);
        if (shift != 0) {
            divisor <<= shift;
            high <<= shift;
            high |= low >>> (64 - shift);
            low <<= shift;
        }

        final long divisorHigh = divisor >>> 32;
        final long divisorLow = divisor & 0xFFFFFFFFL;
        final long lowHigh = low >>> 32;
        final long lowLow = low & 0xFFFFFFFFL;

        // Compute high quotient digit.
        long quotientHigh = Long.divideUnsigned(high, divisorHigh);
        long rhat = Long.remainderUnsigned(high, divisorHigh);

        // qhat >>> 32 == qhat > base
        while ((quotientHigh >>> 32) != 0 || Long.compareUnsigned(quotientHigh * divisorLow, (rhat << 32) | lowHigh) > 0) {
            quotientHigh -= 1;
            rhat += divisorHigh;
            if ((rhat >>> 32) != 0) {
                break;
            }
        }

        long uhat = ((high << 32) | lowHigh) - quotientHigh * divisor;

        // Compute low quotient digit.
        long quotientLow = Long.divideUnsigned(uhat, divisorHigh);
        rhat = Long.remainderUnsigned(uhat, divisorHigh);

        while ((quotientLow >>> 32) != 0 || Long.compareUnsigned(quotientLow * divisorLow, ((rhat << 32) | lowLow)) > 0) {
            quotientLow -= 1;
            rhat += divisorHigh;
            if ((rhat >>> 32) != 0) {
                break;
            }
        }

        low = (uhat << 32 | lowLow) - quotientLow * divisor >>> shift;
        high = 0;

        return new Int128(quotientHigh << 32 | quotientLow, 0);
    }

    private static Int128 divide128by64(long low, long high, long divisor) {
        final int shift = Long.numberOfLeadingZeros(divisor);
        if (shift != 0) {
            divisor <<= shift;
            high <<= shift;
            high |= low >>> (64 - shift);
            low <<= shift;
        }

        final long divisorHigh = divisor >>> 32;
        final long divisorLow = divisor & 0xFFFFFFFFL;
        final long lowHigh = low >>> 32;
        final long lowLow = low & 0xFFFFFFFFL;

        // Compute high quotient digit.
        long quotientHigh = Long.divideUnsigned(high, divisorHigh);
        long rhat = Long.remainderUnsigned(high, divisorHigh);

        // qhat >>> 32 == qhat > base
        while ((quotientHigh >>> 32) != 0 || Long.compareUnsigned(quotientHigh * divisorLow, (rhat << 32) | lowHigh) > 0) {
            quotientHigh -= 1;
            rhat += divisorHigh;
            if ((rhat >>> 32) != 0) {
                break;
            }
        }

        final long uhat = ((high << 32) | lowHigh) - quotientHigh * divisor;

        // Compute low quotient digit.
        long quotientLow = Long.divideUnsigned(uhat, divisorHigh);
        rhat = Long.remainderUnsigned(uhat, divisorHigh);

        while ((quotientLow >>> 32) != 0 || Long.compareUnsigned(quotientLow * divisorLow, ((rhat << 32) | lowLow)) > 0) {
            quotientLow -= 1;
            rhat += divisorHigh;
            if ((rhat >>> 32) != 0) {
                break;
            }
        }

        return new Int128(quotientHigh << 32 | quotientLow, 0);
    }

    // endregion

    // endregion

    // region shift methods

    static long shiftLeftHigh(final Int128 number, final int shift) {
        return shift < 64
                ? (number.high << shift) | (number.low >>> 1 >>> (63 - shift))
                : number.low << (shift - 64);
    }

    public Int128 shiftRightUnsigned(final int shift) {
        return shift < 64
                ? new Int128((high << 1 << (63 - shift)) | (low >>> shift), high >>> shift)
                : new Int128(high >>> (shift - 64), 0);
    }

    public void selfShiftRightUnsigned(final int shift) {
        if (shift < 64) {
            low = (high << 1 << (63 - shift)) | (low >>> shift);
            high = high >>> shift;
        } else {
            low = high >>> (shift - 64);
            high = 0;
        }
    }

    // endregion

    // region additional math

    /**
     * @return 1 if unsigned difference of given {@code minuend} and {@code subtrahend} borrows bit from 9th byte, 0 otherwise
     */
    private static long unsignedBorrow(final long minuend, final long subtrahend) {
        return ((~minuend & subtrahend) | (~(minuend ^ subtrahend) & (minuend - subtrahend))) >>> 63;
    }

    /**
     * @return 1 if unsigned sum of given terms overflows 8 bytes, 0 otherwise
     */
    private static long unsignedCarry(final long term1, final long term2) {
        return ((term1 >>> 1) + (term2 >>> 1) + ((term1 & term2) & 1)) >>> 63;
    }

    /**
     * Branchless form of
     * <pre>
     * if (test < 0) {
     *   return value;
     * }
     * else {
     *   return 0;
     * }
     * </pre>
     */
    private static long ifNegative(final long test, final long value) {
        return value & (test >> 63);
    }

    // TODO: replace with JDK 18's Math.unsignedMultiplyHigh
    private static long unsignedMultiplyHigh(final long x, final long y) {
        // High-Order Product Signed from/to Unsigned
        return Math.multiplyHigh(x, y) + ifNegative(x, y) + ifNegative(y, x);
    }

    // endregion

}