package ru.obukhov.trader.test.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

/**
 * Class for handy creating of {@link OffsetDateTime} instances for tests
 */
@UtilityClass
public class DateTimeTestData {

    // region OffsetDateTime creation

    /**
     * @return OffsetDateTime with by params and UTC zone
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
     * @return OffsetDateTime with by params, 0 nanoseconds and UTC zone
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
     * @return OffsetDateTime with by params, 0 seconds, 0 nanoseconds and UTC zone
     */
    public static OffsetDateTime createDateTime(final int year, final int month, final int dayOfMonth, final int hour, final int minute) {
        return OffsetDateTime.of(year, month, dayOfMonth, hour, minute, 0, 0, DateUtils.DEFAULT_OFFSET);
    }

    /**
     * @return OffsetDateTime with by params, 0 minutes, 0 seconds, 0 nanoseconds and UTC zone
     */
    public static OffsetDateTime createDateTime(final int year, final int month, final int dayOfMonth, final int hour) {
        return OffsetDateTime.of(year, month, dayOfMonth, hour, 0, 0, 0, DateUtils.DEFAULT_OFFSET);
    }

    /**
     * @return OffsetDateTime with by params, 0 hours, 0 minutes, 0 seconds, 0 nanoseconds and UTC zone
     */
    public static OffsetDateTime createDateTime(final int year, final int month, final int dayOfMonth) {
        return OffsetDateTime.of(year, month, dayOfMonth, 0, 0, 0, 0, DateUtils.DEFAULT_OFFSET);
    }

    /**
     * @return OffsetDateTime with by params, 0 hours, 0 minutes, 0 seconds, 0 nanoseconds
     */
    public static OffsetDateTime createDateTime(final int year, final int month, final int dayOfMonth, final ZoneOffset offset) {
        return OffsetDateTime.of(year, month, dayOfMonth, 0, 0, 0, 0, offset);
    }

    /**
     * @return OffsetDateTime with by params, 0 minutes, 0 seconds, 0 nanoseconds
     */
    public static OffsetDateTime createDateTime(final int year, final int month, final int dayOfMonth, final int hour, final ZoneOffset offset) {
        return OffsetDateTime.of(year, month, dayOfMonth, hour, 0, 0, 0, offset);
    }

    // endregion

    /**
     * @return OffsetTime with by params, 0 year, 1 month, 1 day of month, 0 nanoseconds and UTC zone
     */
    public static OffsetTime createTime(final int hour, final int minute, final int second) {
        return OffsetTime.of(hour, minute, second, 0, DateUtils.DEFAULT_OFFSET);
    }

    /**
     * @param year       year of given date
     * @param month      month of given date
     * @param dayOfMonth day of month of given date
     * @return new Interval with {@code from} is start of given date and {@code to} is end of given date
     */
    public static Interval createIntervalOfDay(final int year, final int month, final int dayOfMonth) {
        final OffsetDateTime from = DateTimeTestData.createDateTime(year, month, dayOfMonth);
        final OffsetDateTime to = DateUtils.atEndOfDay(from);
        return Interval.of(from, to);
    }

    /**
     * @return new Interval with {@code from} is start of given date and {@code to} is end of given date
     */
    public static Interval createIntervalOfDay(@NotNull final OffsetDateTime dateTime) {
        final OffsetDateTime from = DateUtils.atStartOfDay(dateTime);
        final OffsetDateTime to = DateUtils.atEndOfDay(from);
        return Interval.of(from, to);
    }

    public static ZoneOffset getNotDefaultOffset() {
        final ZoneOffset defaultOffset = OffsetDateTime.now().getOffset();
        final int totalSeconds = defaultOffset.getTotalSeconds() + (int) TimeUnit.HOURS.toSeconds(1L);
        return ZoneOffset.ofTotalSeconds(totalSeconds);
    }

}