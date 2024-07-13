package ru.obukhov.trader.market.model;

import lombok.Builder;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.RiskLevel;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record Bond(
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
        Integer couponQuantityPerYear,
        OffsetDateTime maturityDate,
        BigDecimal nominal,
        BigDecimal initialNominal,
        OffsetDateTime stateRegDate,
        OffsetDateTime placementDate,
        BigDecimal placementPrice,
        BigDecimal aciValue,
        String countryOfRisk,
        String countryOfRiskName,
        String sector,
        String issueKind,
        Long issueSize,
        Long issueSizePlan,
        SecurityTradingStatus tradingStatus,
        boolean otcFlag,
        boolean buyAvailableFlag,
        boolean sellAvailableFlag,
        boolean floatingCouponFlag,
        boolean perpetualFlag,
        boolean amortizationFlag,
        BigDecimal minPriceIncrement,
        boolean apiTradeAvailableFlag,
        String uid,
        RealExchange realExchange,
        String positionUid,
        boolean forIisFlag,
        boolean forQualInvestorFlag,
        boolean weekendFlag,
        boolean blockedTcaFlag,
        boolean subordinatedFlag,
        boolean liquidityFlag,
        OffsetDateTime first1MinCandleDate,
        OffsetDateTime first1DayCandleDate,
        RiskLevel riskLevel
) implements InstrumentMarker {
}