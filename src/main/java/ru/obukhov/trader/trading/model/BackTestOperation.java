package ru.obukhov.trader.trading.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.Timestamp;
import ru.tinkoff.piapi.contract.v1.OperationType;

import java.math.BigDecimal;

public record BackTestOperation(
        @JsonIgnore String figi,
        Timestamp timestamp,
        OperationType operationType,
        BigDecimal price,
        Long quantity
) {
}