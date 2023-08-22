package ru.obukhov.trader.test.utils.matchers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.mockito.ArgumentMatcher;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.tinkoff.piapi.core.models.Position;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PositionMatcher implements ArgumentMatcher<Position> {

    private final Position expectedPositions;

    public static PositionMatcher of(final Position value) {
        return new PositionMatcher(value);
    }

    @Override
    public boolean matches(final Position actualPosition) {
        return expectedPositions.getFigi().equals(actualPosition.getFigi())
                && expectedPositions.getInstrumentType().equals(actualPosition.getInstrumentType())
                && DecimalUtils.numbersEqual(expectedPositions.getQuantity(), actualPosition.getQuantity())
                && AssertUtils.equals(expectedPositions.getAveragePositionPrice(), actualPosition.getAveragePositionPrice())
                && DecimalUtils.numbersEqual(expectedPositions.getExpectedYield(), actualPosition.getExpectedYield())
                && AssertUtils.equals(expectedPositions.getCurrentNkd(), actualPosition.getCurrentNkd())
                && AssertUtils.equals(expectedPositions.getCurrentPrice(), actualPosition.getCurrentPrice())
                && AssertUtils.equals(expectedPositions.getAveragePositionPriceFifo(), actualPosition.getAveragePositionPriceFifo())
                && DecimalUtils.numbersEqual(expectedPositions.getQuantityLots(), actualPosition.getQuantityLots());
    }

}