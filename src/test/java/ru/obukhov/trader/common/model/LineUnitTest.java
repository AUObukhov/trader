package ru.obukhov.trader.common.model;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.math.BigDecimal;

class LineUnitTest {

    @Test
    void constructor_throwsIllegalArgumentException_whenX1EqualsX2() {
        AssertUtils.assertThrowsWithMessage(
                () -> new Line(10, BigDecimal.ZERO, 10, BigDecimal.ONE),
                IllegalArgumentException.class,
                "x1 and x2 can't be equal"
        );
    }

    @Test
    void getValue_returnsProperValue() {
        final Line line = new Line(10, BigDecimal.valueOf(20), 30, BigDecimal.valueOf(-45));

        final BigDecimal value = line.getValue(20);

        final BigDecimal expectedValue = BigDecimal.valueOf(-12.5);

        AssertUtils.assertEquals(expectedValue, value);
    }

}