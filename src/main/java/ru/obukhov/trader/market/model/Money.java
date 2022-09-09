package ru.obukhov.trader.market.model;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public record Money(
        @NotNull Currency currency,
        @NotNull BigDecimal value
) {

    public static Money of(final Currency currency, BigDecimal value) {
        return new Money(currency, value);
    }

}