package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.util.DateUtils;

import java.time.Duration;
import java.time.OffsetDateTime;

@ExtendWith(MockitoExtension.class)
class RealContextUnitTest {

    @Test
    void getCurrentDateTime_returnsCurrentDateTime() {
        final OffsetDateTime now = DateUtils.now();
        final OffsetDateTime currentTimestamp = new RealContext().getCurrentDateTime();

        final long delay = Duration.between(now, currentTimestamp).toMillis();

        Assertions.assertTrue(delay >= 0);

        final int maxDelay = 5;
        Assertions.assertTrue(delay < maxDelay);
    }

}