package ru.obukhov.trader.market.model;

import lombok.Builder;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.contract.v1.ShareType;

import java.time.OffsetDateTime;

@Builder
public record Share(
        String figi,
        String ticker,
        String classCode,
        String isin,
        Integer lot,
        String currency,
        Quotation klong,
        Quotation kshort,
        Quotation dlong,
        Quotation dshort,
        Quotation dlongMin,
        Quotation dshortMin,
        boolean shortEnabledFlag,
        String name,
        String exchange,
        OffsetDateTime ipoDate,
        Long issueSize,
        String countryOfRisk,
        String countryOfRiskName,
        String sector,
        Long issueSizePlan,
        MoneyValue nominal,
        SecurityTradingStatus tradingStatus,
        boolean otcFlag,
        boolean buyAvailableFlag,
        boolean sellAvailableFlag,
        boolean divYieldFlag,
        ShareType shareType,
        Quotation minPriceIncrement,
        boolean apiTradeAvailableFlag,
        String uid,
        RealExchange realExchange,
        String positionUid,
        boolean forIisFlag,
        boolean forQualInvestorFlag,
        boolean weekendFlag,
        boolean blockedTcaFlag,
        boolean liquidityFlag,
        OffsetDateTime first1MinCandleDate,
        OffsetDateTime first1DayCandleDate
) {
}