package ru.obukhov.trader.market.model;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderStage(
        BigDecimal price,
        long quantity,
        String tradeId
) {
}