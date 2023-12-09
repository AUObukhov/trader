package ru.obukhov.trader.market.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record Dividend(
        BigDecimal dividendNet,
        OffsetDateTime paymentDate,
        OffsetDateTime declaredDate,
        OffsetDateTime lastBuyDate,
        String dividendType,
        OffsetDateTime recordDate,
        String regularity, // "Quarter", "Annual", "Irreg", "Semi-Anl", "Monthly"
        BigDecimal closePrice,
        BigDecimal yieldValue,
        OffsetDateTime createdAt
) {
}