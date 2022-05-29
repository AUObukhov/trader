package ru.obukhov.trader.market.model;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public record MoneyAmount(
        @NotNull String currency,
        @NotNull BigDecimal value
) {
}