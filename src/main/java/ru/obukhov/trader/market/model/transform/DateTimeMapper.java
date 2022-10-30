package ru.obukhov.trader.market.model.transform;

import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import ru.obukhov.trader.common.util.DateUtils;

import java.time.Instant;
import java.time.OffsetDateTime;

@Mapper
public interface DateTimeMapper {

    default OffsetDateTime timestampToOffsetDateTime(final Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        final Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
        return OffsetDateTime.ofInstant(instant, DateUtils.DEFAULT_OFFSET);
    }

    default Timestamp offsetDateTimeToTimestamp(final OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        final Instant instant = dateTime.toInstant();
        return Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
    }

    default Timestamp instantToTimestamp(final Instant instant) {
        if (instant == null) {
            return null;
        }

        return Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
    }

}