package ru.obukhov.trader.trading.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.tinkoff.piapi.contract.v1.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BackTestOperation(
        @JsonIgnore String ticker,
        OffsetDateTime dateTime,
        OperationType operationType,
        BigDecimal price,
        Long quantity
) {
}