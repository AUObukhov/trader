package ru.obukhov.investor.model.transform;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.investor.web.model.SimulatedOperation;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.MoneyAmount;
import ru.tinkoff.invest.openapi.models.operations.Operation;
import ru.tinkoff.invest.openapi.models.operations.OperationStatus;
import ru.tinkoff.invest.openapi.models.operations.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OperationMapperTest {

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);

    @Test
    public void mapsOperationToSimulatedOperation() {
        OffsetDateTime date = OffsetDateTime.now();
        BigDecimal commission = BigDecimal.TEN;
        MoneyAmount commissionMoneyAmount = new MoneyAmount(Currency.RUB, commission);
        Operation source = new Operation(StringUtils.EMPTY,
                OperationStatus.Done,
                null,
                commissionMoneyAmount,
                Currency.RUB,
                BigDecimal.ZERO,
                BigDecimal.TEN,
                2,
                null,
                null,
                false,
                date,
                null);

        SimulatedOperation target = operationMapper.map(source);

        assertEquals(source.date, target.getDateTime());
        assertEquals(source.operationType, target.getOperationType());
        assertEquals(source.price, target.getPrice());
        assertEquals(source.quantity, target.getQuantity());
        assertEquals(source.commission.value, target.getCommission());

    }

    @Test
    public void mapsSimulatedOperationToOperation() {
        SimulatedOperation source = SimulatedOperation.builder()
                .dateTime(OffsetDateTime.now())
                .operationType(OperationType.Buy)
                .price(BigDecimal.TEN)
                .quantity(2)
                .commission(BigDecimal.ONE)
                .build();

        Operation target = operationMapper.map(source);

        assertEquals(source.getDateTime(), target.date);
        assertEquals(source.getOperationType(), target.operationType);
        assertEquals(source.getPrice(), target.price);
        assertEquals(source.getQuantity(), target.quantity);
        assertEquals(source.getCommission(), target.commission.value);
    }

}