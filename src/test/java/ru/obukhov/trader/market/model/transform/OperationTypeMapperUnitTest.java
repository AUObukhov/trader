package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.OperationTypeWithCommission;

class OperationTypeMapperUnitTest {

    private final OperationTypeMapper operationTypeMapper = Mappers.getMapper(OperationTypeMapper.class);

    @Test
    void mapsBuyOperation() {
        final OperationType source = OperationType.BUY;

        final OperationTypeWithCommission target = operationTypeMapper.map(source);

        Assertions.assertEquals(OperationTypeWithCommission.BUY, target);
    }

    @Test
    void mapsSellOperation() {
        final OperationType source = OperationType.SELL;

        final OperationTypeWithCommission target = operationTypeMapper.map(source);

        Assertions.assertEquals(OperationTypeWithCommission.SELL, target);
    }

}