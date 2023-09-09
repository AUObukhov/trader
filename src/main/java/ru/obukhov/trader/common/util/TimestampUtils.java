package ru.obukhov.trader.common.util;

import com.google.protobuf.Timestamp;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * Utils for {@link Timestamp}
 */
@UtilityClass
public class TimestampUtils {

    public static final ZoneOffset DEFAULT_OFFSET = ZoneOffset.of("+03:00");

    private static final long SECONDS_MIN_VALUE = -62135596800L;
    private static final long SECONDS_MAX_VALUE = 253402300799L;
    private static final int NANOS_MIN_VALUE = 0;
    private static final int NANOS_MAX_VALUE = 999_999_999;

    // region newTimestamp

    public static Timestamp newTimestamp(final long seconds) {
        return newTimestamp(seconds, 0);
    }

    public static Timestamp newTimestamp(final long seconds, final int nanos) {
        if (seconds < SECONDS_MIN_VALUE || seconds > SECONDS_MAX_VALUE) {
            throw new IllegalArgumentException(
                    "seconds must be from 0001-01-01T00:00:00Z (" + SECONDS_MIN_VALUE +
                            ") to 9999-12-31T23:59:59Z (" + SECONDS_MAX_VALUE + ") inclusive"
            );
        }
        if (nanos < NANOS_MIN_VALUE || nanos > NANOS_MAX_VALUE) {
            throw new IllegalArgumentException("nanos must be from " + NANOS_MIN_VALUE + " to " + NANOS_MAX_VALUE + " inclusive");
        }

        return Timestamp.newBuilder().setSeconds(seconds).setNanos(nanos).build();
    }

    public static Timestamp newTimestamp(final int year, final int month, final int dayOfMonth, final int hour) {
        return newTimestamp(year, month, dayOfMonth, hour, 0, 0, 0, DateUtils.DEFAULT_OFFSET);
    }

    public static Timestamp newTimestamp(final int year, final int month, final int dayOfMonth, final int hour, final int minute) {
        return newTimestamp(year, month, dayOfMonth, hour, minute, 0, 0, DateUtils.DEFAULT_OFFSET);
    }

    public static Timestamp newTimestamp(final int year, final int month, final int dayOfMonth, final int hour, final int minute, final int second) {
        return newTimestamp(year, month, dayOfMonth, hour, minute, second, 0, DateUtils.DEFAULT_OFFSET);
    }

    public static Timestamp newTimestamp(
            final int year,
            final int month,
            final int dayOfMonth,
            final int hour,
            final int minute,
            final int second,
            final int nanoOfSecond
    ) {
        return newTimestamp(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, DateUtils.DEFAULT_OFFSET);
    }

    public static Timestamp newTimestamp(
            final int year,
            final int month,
            final int dayOfMonth,
            final int hour,
            final int minute,
            final int second,
            final int nanoOfSecond,
            final ZoneOffset offset
    ) {
        final OffsetDateTime dateTime = OffsetDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, offset);
        return newTimestamp(dateTime);
    }

    public static Timestamp newTimestamp(final OffsetDateTime dateTime) {
        final Instant instant = dateTime.toInstant();
        return newTimestamp(instant.getEpochSecond(), instant.getNano());
    }

    // endregion

    // region conversion

    public static Instant toInstant(final Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    public static OffsetDateTime toOffsetDateTime(final Timestamp timestamp) {
        return toInstant(timestamp).atOffset(DEFAULT_OFFSET);
    }

    public static Date toDate(final Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        return Date.from(toInstant(timestamp));
    }

    // endregion

}