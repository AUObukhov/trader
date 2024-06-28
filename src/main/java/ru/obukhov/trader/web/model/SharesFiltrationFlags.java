package ru.obukhov.trader.web.model;

import lombok.With;

@With
public record SharesFiltrationFlags(
        boolean filterByCurrency,
        boolean filterByApiTradeAvailableFlag,
        boolean filterByForQualInvestorFlag,
        boolean filterByForIisFlag,
        boolean filterByShareType,
        boolean filterByTradingPeriod,
        boolean filterByHavingDividends,
        boolean filterByHavingRecentDividends,
        boolean filterByRegularInvestingAnnualReturns
) {
}