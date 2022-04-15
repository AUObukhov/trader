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

        final BackTestOperation target = operationMapper.map(source);

        Assertions.assertEquals(source.date(), target.getDateTime());
        Assertions.assertEquals(OperationType.BUY, target.getOperationType());
        Assertions.assertEquals(source.price(), target.getPrice());
        Assertions.assertEquals(source.quantity(), target.getQuantity());
        Assertions.assertEquals(source.commission().value(), target.getCommission());
    }

    @Test
    void mapsBackTestOperationToOperation() {
        final BackTestOperation source = BackTestOperation.builder()
                .dateTime(OffsetDateTime.now())
                .operationType(OperationType.BUY)
                .price(BigDecimal.TEN)
                .quantity(2)
                .commission(BigDecimal.ONE)
                .build();

        final Operation target = operationMapper.map(source);

        Assertions.assertEquals(source.getDateTime(), target.date());
        Assertions.assertEquals(OperationTypeWithCommission.BUY, target.operationType());
        Assertions.assertEquals(source.getPrice(), target.price());
        Assertions.assertEquals(source.getQuantity(), target.quantity());
        Assertions.assertEquals(source.getCommission(), target.commission().value());
    }

}