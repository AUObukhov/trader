package ru.obukhov.trader.market.model;

import java.math.BigDecimal;

public record CurrencyPosition(Currency currency, BigDecimal balance, BigDecimal blocked) {
}
