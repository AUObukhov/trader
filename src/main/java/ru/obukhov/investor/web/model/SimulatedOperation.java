package ru.obukhov.investor.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.tinkoff.invest.openapi.models.operations.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class SimulatedOperation {

    private OffsetDateTime dateTime;

    private OperationType operationType;

    private BigDecimal amount;

    private BigDecimal commission;

}