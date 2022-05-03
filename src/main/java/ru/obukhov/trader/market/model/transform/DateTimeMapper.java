package ru.obukhov.trader.market.model.transform;

import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;
import ru.obukhov.trader.common.util.DateUtils;

import java.time.Instant;
import java.time.OffsetDateTime;

@Mapper
public interface DateTimeMapper {

    default OffsetDateTime map(final Timestamp timestamp) {
        final Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
        return OffsetDateTime.ofInstant(instant, DateUtils.DEFAULT_OFFSET);
    }

    default Timestamp map(final OffsetDateTime dateTime) {
        final Instant instant = dateTime.toInstant();
        return Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
    }

}