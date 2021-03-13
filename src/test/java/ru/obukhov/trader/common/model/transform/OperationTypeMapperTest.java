package ru.obukhov.trader.common.model.transform;

import org.junit.Assert;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.transform.OperationTypeMapper;
import ru.tinkoff.invest.openapi.models.operations.OperationType;
import ru.tinkoff.invest.openapi.models.orders.Operation;

public class OperationTypeMapperTest {

    private final OperationTypeMapper operationTypeMapper = Mappers.getMapper(OperationTypeMapper.class);

    @Test
    public void mapsBuyOperation() {
        Operation source = Operation.Buy;

        OperationType target = operationTypeMapper.map(source);

        Assert.assertEquals(OperationType.Buy, target);
    }

    @Test
    public void mapsSellOperation() {
        Operation source = Operation.Sell;

        OperationType target = operationTypeMapper.map(source);

        Assert.assertEquals(OperationType.Sell, target);
    }

}