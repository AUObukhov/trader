package ru.obukhov.trader.trading.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.obukhov.trader.market.model.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BackTestOperation(
        @JsonIgnore String ticker,
        OffsetDateTime dateTime,
        OperationType operationType,
        BigDecimal price,
        Integer quantity,
        BigDecimal commission
) {
}