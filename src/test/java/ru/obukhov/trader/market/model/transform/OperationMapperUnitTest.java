package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.trading.model.BackTestOperation;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

class OperationMapperUnitTest {

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);

    @Test
    void mapsOperationToBackTestOperation() {
        final Operation source = TestData.createOperation(OffsetDateTime.now(), OperationType.OPERATION_TYPE_BUY, 10, 2);

        final BackTestOperation target = operationMapper.map(null, source);

        AssertUtils.assertEquals(source.getDate(), target.dateTime());
        Assertions.assertEquals(OperationType.OPERATION_TYPE_BUY, target.operationType());
        AssertUtils.assertEquals(source.getPrice(), target.price());
        Assertions.assertEquals(source.getQuantity(), target.quantity());
    }

    @Test
    void mapsBackTestOperationToOperation() {
        final BackTestOperation source = new BackTestOperation(null, OffsetDateTime.now(), OperationType.OPERATION_TYPE_BUY, BigDecimal.TEN, 2L);

        final Operation target = operationMapper.map(source);

        AssertUtils.assertEquals(source.dateTime(), target.getDate());
        Assertions.assertEquals(OperationType.OPERATION_TYPE_BUY, target.getOperationType());
        AssertUtils.assertEquals(source.price(), target.getPrice());
        Assertions.assertEquals(source.quantity(), target.getQuantity());
    }

}