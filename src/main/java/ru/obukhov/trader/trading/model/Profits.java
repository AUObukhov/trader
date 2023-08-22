package ru.obukhov.trader.trading.model;

import ru.obukhov.trader.common.util.QuotationUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

/**
 * Container of profits
 */
public record Profits(
        Quotation absolute, // Absolute profit amount
        double relative, // Relative profit - relation of absolute profit to investments amount
        double relativeAnnual // Average profitability per year
) {
    /**
     * Instance of the class with zero values of all profits
     */
    public static final Profits ZEROS = new Profits(QuotationUtils.ZERO, 0.0, 0.0);

}