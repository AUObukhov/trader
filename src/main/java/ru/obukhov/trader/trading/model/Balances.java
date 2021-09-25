package ru.obukhov.trader.trading.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Balances {

    /**
     * Initial investment
     */
    private BigDecimal initialInvestment;

    /**
     * Sum of all investments
     */
    private BigDecimal totalInvestment;

    /**
     * Weighted average value of all investments where weight is time of corresponding investment being last investment
     */
    private BigDecimal weightedAverageInvestment;

    /**
     * Currency balance after back test
     */
    private BigDecimal finalBalance;

    /**
     * {@code finalBalance} + costs of all positions after back test
     */
    private BigDecimal finalTotalSavings;

}