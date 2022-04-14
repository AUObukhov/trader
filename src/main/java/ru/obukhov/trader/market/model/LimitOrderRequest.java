package ru.obukhov.trader.market.model;

import java.math.BigDecimal;

public record LimitOrderRequest(Integer lots, OperationType operation, BigDecimal price) {
}