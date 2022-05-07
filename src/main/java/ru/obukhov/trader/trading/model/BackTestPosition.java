package ru.obukhov.trader.trading.model;

import java.math.BigDecimal;

public record BackTestPosition(String ticker, BigDecimal price, long quantity) {
}