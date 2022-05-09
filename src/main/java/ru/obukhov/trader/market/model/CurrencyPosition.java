package ru.obukhov.trader.market.model;

import java.math.BigDecimal;

public record CurrencyPosition(String currency, BigDecimal balance, BigDecimal blocked) {
}
