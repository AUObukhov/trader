package ru.obukhov.trader.market.model;

import lombok.Builder;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record Instrument(
        String figi,
        String ticker,
        String classCode,
        String isin,
        int lotSize,
        Currency currency,
        BigDecimal kLong,
        BigDecimal kShort,
        BigDecimal dLong,
        BigDecimal dShort,
        BigDecimal dLongMin,
        BigDecimal dShortMin,
        boolean shortEnabled,
        String exchange,
        String countryOfRisk,
        String countryOfRiskName,
        String instrumentType,
        SecurityTradingStatus tradingStatus,
        boolean otcFlag,
        boolean buyAvailable,
        boolean sellAvailable,
        BigDecimal minPriceIncrement,
        boolean apiTradeAvailable,
        String uid,
        RealExchange realExchange,
        String positionUid,
        boolean availableForIis,
        boolean forQualInvestor,
        boolean availableOnWeekend,
        boolean blockedTca,
        InstrumentType instrumentKind,
        OffsetDateTime first1MinCandleDate,
        OffsetDateTime first1DayCandleDate
) {
}