package ru.obukhov.trader.trading.model;

import java.math.BigDecimal;

public record Balances(
        BigDecimal initialInvestment, // Initial investment
        BigDecimal totalInvestment, // Sum of all investments
        BigDecimal weightedAverageInvestment,
        // Weighted average value of all investments where weight is time of corresponding investment being last investment
        BigDecimal finalBalance, // Currency balance after back test
        BigDecimal finalTotalSavings // {@code finalBalance} + costs of all positions after back test
) {
}