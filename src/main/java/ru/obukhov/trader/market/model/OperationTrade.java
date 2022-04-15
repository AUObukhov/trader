package ru.obukhov.trader.market.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OperationTrade(String tradeId, OffsetDateTime date, BigDecimal price, Integer quantity) {
}