package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Operation;
import ru.obukhov.trader.market.model.OperationType;
import ru.obukhov.trader.market.model.OperationTypeWithCommission;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.trading.model.BackTestOperation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

class OperationMapperUnitTest {

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);

    @Test
    void mapsOperationToBackTestOperation() {
        final Operation source = TestData.createOperation(
                OffsetDateTime.now(),
                OperationTypeWithCommission.BUY,
                10,
                2,
                1
        );

        final BackTestOperation target = operationMapper.map(null, source);

        Assertions.assertEquals(source.date(), target.dateTime());
        Assertions.assertEquals(OperationType.BUY, target.operationType());
        Assertions.assertEquals(source.price(), target.price());
        Assertions.assertEquals(source.quantity(), target.quantity());
        Assertions.assertEquals(source.commission().value(), target.commission());
    }

    @Test
    void mapsBackTestOperationToOperation() {
        final BackTestOperation source = new BackTestOperation(
                null,
                OffsetDateTime.now(),
                OperationType.BUY,
                BigDecimal.TEN,
                2,
                BigDecimal.ONE
        );

        final Operation target = operationMapper.map(source);

        Assertions.assertEquals(source.dateTime(), target.date());
        Assertions.assertEquals(OperationTypeWithCommission.BUY, target.operationType());
        Assertions.assertEquals(source.price(), target.price());
        Assertions.assertEquals(source.quantity(), target.quantity());
        Assertions.assertEquals(source.commission(), target.commission().value());
    }

}