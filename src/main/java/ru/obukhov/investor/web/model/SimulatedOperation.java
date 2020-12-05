package ru.obukhov.investor.web.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tinkoff.invest.openapi.models.operations.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class SimulatedOperation {

    private OffsetDateTime dateTime;

    private OperationType operationType;

    private BigDecimal amount;

    private BigDecimal commission;

}