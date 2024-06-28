package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.model.dividend.TestDividends;

import java.time.OffsetDateTime;
import java.util.stream.Stream;

class DividendTest {

    private static final Dividend DIVIDEND = TestDividends.TEST_DIVIDEND1.dividend();

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forIsBefore() {
        return Stream.of(
                Arguments.of(DIVIDEND.lastBuyDate().minusNanos(1), false),
                Arguments.of(DIVIDEND.lastBuyDate(), false),
                Arguments.of(DIVIDEND.lastBuyDate().plusNanos(1), true)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forIsBefore")
    void isBefore(final OffsetDateTime dateTime, final boolean expectedResult) {
        Assertions.assertEquals(expectedResult, DIVIDEND.isBefore(dateTime));
    }

}