package ru.obukhov.trader.trading.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Container of profits
 */
@Data
@AllArgsConstructor
public class Profits {

    /**
     * Creates a new instance of the class with zero values of all profits
     */
    public static final Profits ZEROS = new Profits(BigDecimal.ZERO, 0.0, 0.0);

    /**
     * Absolute profit amount
     */
    private BigDecimal absolute;

    /**
     * Relative profit - relation of absolute profit to investments amount
     */
    private double relative;

    /**
     * Average profitability per annum
     */
    private double averageAnnualProfitability;

}