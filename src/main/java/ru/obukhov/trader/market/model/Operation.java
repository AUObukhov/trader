package ru.obukhov.trader.market.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record Operation(
        String id,
        OperationStatus status,
        List<OperationTrade> trades,
        MoneyAmount commission,
        Currency currency,
        BigDecimal payment,
        BigDecimal price,
        Integer quantity,
        Integer quantityExecuted,
        String figi,
        InstrumentType instrumentType,
        Boolean isMarginCall,
        OffsetDateTime date,
        OperationTypeWithCommission operationType
) {
}