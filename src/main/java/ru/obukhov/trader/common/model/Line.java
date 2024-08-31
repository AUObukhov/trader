package ru.obukhov.trader.common.model;

import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.DecimalUtils;

import java.math.BigDecimal;

/**
 * Class, representing line on plane, described by equation {@code Ax + By + C = 0}, where:<br/>
 * x - index of element in collection<br/>
 * y - {@link BigDecimal} value of element
 */
public class Line {

    private final BigDecimal a;
    private final BigDecimal b;
    private final BigDecimal c;

    public Line(final int x1, final BigDecimal y1, final int x2, final BigDecimal y2) {
        Assert.isTrue(x1 != x2, "x1 and x2 can't be equal");

        this.a = y2.subtract(y1); // a is actually y1 - y2, but it is always used with opposite sign, so it is inverted from the beginning
        this.b = BigDecimal.valueOf((long) x2 - x1);
        this.c = DecimalUtils.multiply(y2, x1).subtract(DecimalUtils.multiply(y1, x2));
    }

    public BigDecimal getValue(final int x) {
        return DecimalUtils.divide(DecimalUtils.multiply(a, x).subtract(c), b);
    }

}