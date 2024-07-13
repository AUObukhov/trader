package ru.obukhov.trader.web.model;

import lombok.With;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@With
public record SharesFiltrationOptions(
        @Nullable List<String> currencies,
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