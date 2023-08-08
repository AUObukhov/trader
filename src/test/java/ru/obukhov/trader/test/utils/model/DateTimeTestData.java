package ru.obukhov.trader.test.utils.model;

import lombok.experimental.UtilityClass;
import ru.obukhov.trader.common.util.DateUtils;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

/**
 * Class for handy creating of {@link OffsetDateTime} instances for tests
 */
@UtilityClass
public class DateTimeTestData {
    private static final int NANOS_MAX_VALUE = 999_999_999;

    // region OffsetDateTime creation

    /**
     * @return OffsetDateTime with by params and default offset
     */
    public static OffsetDateTime createDateTime(
            final int year,
            final int month,
            final int dayOfMonth,
            final int hour,
            final int minute,
            final int second,
            final int nanoOfSecond
    ) {
        return OffsetDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, DateUtils.DEFAULT_OFFSET);
    }

    /**
     * @return OffsetDateTime with by params, 0 nanoseconds and default offset
     */
    public static OffsetDateTime createDateTime(
            final int year,
            final int month,
            final int dayOfMonth,
            final int hour,
            final int minute,
            final int second
    ) {
        return OffsetDateTime.of(year, month, dayOfMonth, hour, minute, second, 0, DateUtils.DEFAULT_OFFSET);
    }

    /**
     * @return OffsetDateTime with by params, 0 seconds, 0 nanoseconds and default offset
     */
    public static OffsetDateTime createDateTime(final int year, final int month, final int dayOfMonth, final int hour, final int minute) {
        return OffsetDateTime.of(year, month, dayOfMonth, hour, minute, 0, 0, DateUtils.DEFAULT_OFFSET);
    }

    /**
     * @return OffsetDateTime with by params, 0 minutes, 0 seconds, 0 nanoseconds and default offset
     */
    public static OffsetDateTime createDateTime(final int year, final int month, final int dayOfMonth, final int hour) {
        return OffsetDateTime.of(year, month, dayOfMonth, hour, 0, 0, 0, DateUtils.DEFAULT_OFFSET);
    }

    /**
     * @return OffsetDateTime with by params, 0 hours, 0 minutes, 0 seconds, 0 nanoseconds and default offset
     */
    public static OffsetDateTime createDateTime(final int year, final int month, final int dayOfMonth) {
        return OffsetDateTime.of(year, month, dayOfMonth, 0, 0, 0, 0, DateUtils.DEFAULT_OFFSET);
    }

    /**
     * @return OffsetDateTime with by params, 0 hours, 0 minutes, 0 seconds, 0 nanoseconds and default offset
     */
    public static OffsetDateTime createDateTime(final int year) {
        return OffsetDateTime.of(year, 1, 1, 0, 0, 0, 0, DateUtils.DEFAULT_OFFSET);
    }

    /**
     * @return OffsetDateTime with by params, 0 seconds, 0 nanoseconds
     */
    public static OffsetDateTime createDateTime(
            final int year,
            final int month,
            final int dayOfMonth,
            final int hour,
            final int minute,
            final ZoneOffset offset
    ) {
        return OffsetDateTime.of(year, month, dayOfMonth, hour, minute, 0, 0, offset);
    }

    /**
     * @return OffsetDateTime with by params, 0 minutes, 0 seconds, 0 nanoseconds
     */
    public static OffsetDateTime createDateTime(final int year, final int month, final int dayOfMonth, final int hour, final ZoneOffset offset) {
        return OffsetDateTime.of(year, month, dayOfMonth, hour, 0, 0, 0, offset);
    }

    /**
     * @return OffsetDateTime with by params, 0 hours, 0 minutes, 0 seconds, 0 nanoseconds
     */
    public static OffsetDateTime createDateTime(final int year, final int month, final int dayOfMonth, final ZoneOffset offset) {
        return OffsetDateTime.of(year, month, dayOfMonth, 0, 0, 0, 0, offset);
    }

    public static OffsetDateTime createEndOfDay(final int year, final int month, final int dayOfMonth) {
        return OffsetDateTime.of(year, month, dayOfMonth, 23, 59, 59, NANOS_MAX_VALUE, DateUtils.DEFAULT_OFFSET);
    }

    // endregion

    /**
     * @return OffsetTime with by params, 0 year, 1 month, 1 day of month, 0 nanoseconds and default offset
     */
    public static OffsetTime createTime(final int hour, final int minute, final int second) {
        return OffsetTime.of(hour, minute, second, 0, DateUtils.DEFAULT_OFFSET);
    }

}