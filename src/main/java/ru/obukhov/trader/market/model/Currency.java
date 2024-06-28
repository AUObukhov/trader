package ru.obukhov.trader.market.model;

import lombok.Builder;
import lombok.With;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@With
@Builder
public record Currency(
        String figi,
        String ticker,
        String classCode,
        String isin,
        Integer lot,
        String currency,
        BigDecimal klong,
        BigDecimal kshort,
        BigDecimal dlong,
        BigDecimal dshort,
        BigDecimal dlongMin,
        BigDecimal dshortMin,
        boolean shortEnabledFlag,
        String name,
        String exchange,
        BigDecimal nominal,
        String countryOfRisk,
        String countryOfRiskName,
        SecurityTradingStatus tradingStatus,
        boolean otcFlag,
        boolean buyAvailableFlag,
        boolean sellAvailableFlag,
        String isoCurrencyName,
        BigDecimal minPriceIncrement,
        boolean apiTradeAvailableFlag,
        String uid,
        RealExchange realExchange,
        String positionUid,
        boolean forIisFlag,
        boolean forQualInvestorFlag,
        boolean weekendFlag,
        boolean blockedTcaFlag,
        OffsetDateTime first1MinCandleDate,
        OffsetDateTime first1DayCandleDate
) {
}