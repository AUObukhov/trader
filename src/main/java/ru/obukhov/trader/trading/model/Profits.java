package ru.obukhov.trader.trading.model;

import java.math.BigDecimal;

public record Profits(
        BigDecimal absolute, // Absolute profit amount
        double relative, // Relative profit - relation of absolute profit to investments amount
        double relativeAnnual // Average profitability per year
) {
}