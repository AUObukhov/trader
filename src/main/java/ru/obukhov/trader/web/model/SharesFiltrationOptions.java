package ru.obukhov.trader.web.model;

import lombok.With;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.piapi.contract.v1.ShareType;

import java.util.List;

@With
public record SharesFiltrationOptions(
        @Nullable List<String> currencies,
        @Nullable Boolean apiTradeAvailableFlag,
        @Nullable Boolean forQualInvestorFlag,
        @Nullable Boolean forIisFlag,
        @Nullable List<ShareType> shareTypes,
        @Nullable Integer minTradingDays,
        @Nullable Integer havingDividendsWithinDays,
        boolean filterByRegularInvestingAnnualReturns
) {
}