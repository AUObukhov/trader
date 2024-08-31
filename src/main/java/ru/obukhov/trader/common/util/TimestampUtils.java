package ru.obukhov.trader.common.util;

import com.google.protobuf.Timestamp;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@UtilityClass
public class TimestampUtils {

    public static final ZoneOffset DEFAULT_OFFSET = ZoneOffset.of("+03:00");

    // region conversion

    public static Instant toInstant(final Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    public static OffsetDateTime toOffsetDateTime(final Timestamp timestamp) {
        return toInstant(timestamp).atOffset(DEFAULT_OFFSET);
    }

    // endregion

}