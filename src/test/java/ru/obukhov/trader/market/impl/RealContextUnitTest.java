package ru.obukhov.trader.market.impl;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.util.TimestampUtils;

@ExtendWith(MockitoExtension.class)
class RealContextUnitTest {

    @Test
    void getCurrentDateTime_returnsCurrentDateTime() {
        final Timestamp now = TimestampUtils.now();
        final Timestamp currentTimestamp = new RealContext().getCurrentTimestamp();

        final long delay = TimestampUtils.toDuration(now, currentTimestamp).toMillis();

        Assertions.assertTrue(delay >= 0);

        final int maxDelay = 5;
        Assertions.assertTrue(delay < maxDelay);
    }

}