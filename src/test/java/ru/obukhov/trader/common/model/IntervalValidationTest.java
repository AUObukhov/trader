package ru.obukhov.trader.common.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

import java.time.OffsetDateTime;
import java.util.stream.Stream;

class IntervalValidationTest {

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forValidationSucceeds() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 1, 1),
                        DateTimeTestData.newDateTime(2023, 1, 2)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 1, 1),
                        null
                ),
                Arguments.of(
                        null,
                        DateTimeTestData.newDateTime(2023, 1, 2)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forValidationSucceeds")
    void validationSucceeds(final OffsetDateTime from, final OffsetDateTime to) {
        final Interval interval = Interval.of(from, to);

        AssertUtils.assertNoViolations(interval);
    }

    @Test
    void validationFails_whenFromAndToAreNull() {
        final Interval interval = Interval.of(null, null);

        AssertUtils.assertViolation(interval, "from and to can't be both null");
    }

}