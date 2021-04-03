package ru.obukhov.trader.common.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.transform.OperationMapper;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;
import ru.tinkoff.invest.openapi.model.rest.MoneyAmount;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.OperationTypeWithCommission;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

class OperationMapperUnitTest {

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);

    @Test
    void mapsOperationToSimulatedOperation() {
        Operation source = new Operation();

        MoneyAmount commission = new MoneyAmount();
        commission.setValue(BigDecimal.ONE);
        source.setCommission(commission);

        source.setPrice(BigDecimal.TEN);
        source.setQuantity(2);
        source.setDate(OffsetDateTime.now());
        source.setOperationType(OperationTypeWithCommission.BUY);

        SimulatedOperation target = operationMapper.map(source);

        Assertions.assertEquals(source.getDate(), target.getDateTime());
        Assertions.assertEquals(OperationType.BUY, target.getOperationType());
        Assertions.assertEquals(source.getPrice(), target.getPrice());
        Assertions.assertEquals(source.getQuantity(), target.getQuantity());
        Assertions.assertEquals(source.getCommission().getValue(), target.getCommission());

    }

    @Test
    void mapsSimulatedOperationToOperation() {
        SimulatedOperation source = SimulatedOperation.builder()
                .dateTime(OffsetDateTime.now())
                .operationType(OperationType.BUY)
                .price(BigDecimal.TEN)
                .quantity(2)
                .commission(BigDecimal.ONE)
                .build();

        Operation target = operationMapper.map(source);

        Assertions.assertEquals(source.getDateTime(), target.getDate());
        Assertions.assertEquals(OperationTypeWithCommission.BUY, target.getOperationType());
        Assertions.assertEquals(source.getPrice(), target.getPrice());
        Assertions.assertEquals(source.getQuantity(), target.getQuantity());
        Assertions.assertEquals(source.getCommission(), target.getCommission().getValue());
    }

}