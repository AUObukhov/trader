package ru.obukhov.trader.trading.model;

import java.math.BigDecimal;

/**
 * Container of profits
 */
public record Profits(
        BigDecimal absolute, // Absolute profit amount
        double relative, // Relative profit - relation of absolute profit to investments amount
        double relativeAnnual // Average profitability per year
) {
    /**
     * Instance of the class with zero values of all profits
     */
    public static final Profits ZEROS = new Profits(BigDecimal.ZERO, 0.0, 0.0);

}