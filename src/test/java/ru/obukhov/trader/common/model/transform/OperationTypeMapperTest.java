package ru.obukhov.trader.common.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.transform.OperationTypeMapper;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.OperationTypeWithCommission;

class OperationTypeMapperTest {

    private final OperationTypeMapper operationTypeMapper = Mappers.getMapper(OperationTypeMapper.class);

    @Test
    void mapsBuyOperation() {
        OperationType source = OperationType.BUY;

        OperationTypeWithCommission target = operationTypeMapper.map(source);

        Assertions.assertEquals(OperationTypeWithCommission.BUY, target);
    }

    @Test
    void mapsSellOperation() {
        OperationType source = OperationType.SELL;

        OperationTypeWithCommission target = operationTypeMapper.map(source);

        Assertions.assertEquals(OperationTypeWithCommission.SELL, target);
    }

}