package ru.obukhov.trader.market.model;

import lombok.Builder;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record Etf(
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
        BigDecimal fixedCommission,
        String focusType,
        OffsetDateTime releasedDate,
        BigDecimal numShares,
        String countryOfRisk,
        String countryOfRiskName,
        String sector,
        String rebalancingFreq,
        SecurityTradingStatus tradingStatus,
        boolean otcFlag,
        boolean buyAvailableFlag,
        boolean sellAvailableFlag,
        BigDecimal minPriceIncrement,
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
) implements InstrumentMarker {
}