package ru.obukhov.trader.common.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.market.model.MovingAverageType;

class MovingAverageTypeConverterUnitTest {

    @Test
    void convert() {
        final MovingAverageTypeConverter converter = new MovingAverageTypeConverter();
        for (final MovingAverageType movingAverageType : MovingAverageType.values()) {
            Assertions.assertEquals(movingAverageType, converter.convert(movingAverageType.getValue()));
        }
    }

}