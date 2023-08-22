package ru.obukhov.trader.trading.model;

import ru.tinkoff.piapi.contract.v1.Quotation;

public record Balances(
        // Initial investment
        Quotation initialInvestment,

        // Sum of all investments
        Quotation totalInvestment,

        // Weighted average value of all investments where weight is time of corresponding investment being last investment
        Quotation weightedAverageInvestment,

        // Currency balance after back test
        Quotation finalBalance,

        // finalBalance + costs of all positions after back test
        Quotation finalTotalSavings
) {
}