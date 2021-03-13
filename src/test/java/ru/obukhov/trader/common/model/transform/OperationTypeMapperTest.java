package ru.obukhov.trader.common.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.transform.OperationTypeMapper;
import ru.tinkoff.invest.openapi.models.operations.OperationType;
import ru.tinkoff.invest.openapi.models.orders.Operation;

class OperationTypeMapperTest {

    private final OperationTypeMapper operationTypeMapper = Mappers.getMapper(OperationTypeMapper.class);

    @Test
    void mapsBuyOperation() {
        Operation source = Operation.Buy;

        OperationType target = operationTypeMapper.map(source);

        Assertions.assertEquals(OperationType.Buy, target);
    }

    @Test
    void mapsSellOperation() {
        Operation source = Operation.Sell;

        OperationType target = operationTypeMapper.map(source);

        Assertions.assertEquals(OperationType.Sell, target);
    }

}