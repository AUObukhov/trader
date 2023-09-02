package ru.obukhov.trader.trading.model;

import java.math.BigDecimal;

public record Balances(
        // Initial investment
        BigDecimal initialInvestment,

        // Sum of all investments
        BigDecimal totalInvestment,

        // Weighted average value of all investments where weight is time of corresponding investment being last investment
        BigDecimal weightedAverageInvestment,

        // Currency balance after back test
        BigDecimal finalBalance,

        // finalBalance + costs of all positions after back test
        BigDecimal finalTotalSavings
) {
}