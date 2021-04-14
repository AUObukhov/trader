package ru.obukhov.trader.common.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.trader.common.util.DecimalUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Trend point
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Point {

    private OffsetDateTime time;

    private BigDecimal value;

    public static Point of(OffsetDateTime time, BigDecimal value) {
        return new Point(time, DecimalUtils.setDefaultScale(value));
    }

    public static Point of(OffsetDateTime time, Double value) {
        return new Point(time, DecimalUtils.setDefaultScale(value));
    }

    public static Point of(OffsetDateTime time, Integer value) {
        return new Point(time, DecimalUtils.setDefaultScale(value));
    }

}