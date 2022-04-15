package ru.obukhov.trader.market.model;

import java.math.BigDecimal;

public record OrderResponse(BigDecimal price, Integer quantity) {
}