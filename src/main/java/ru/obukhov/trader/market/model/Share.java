package ru.obukhov.trader.market.model;

import lombok.Builder;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record Share(
        String figi,
        String ticker,
        Integer lotSize,
        Currency currency,
        String name,
        Exchange exchange,
        OffsetDateTime ipoDate,
        Long issueSize,
        String country,
        Sector sector,
        Long issueSizePlan,
        BigDecimal nominal,
        SecurityTradingStatus tradingStatus,
        boolean buyAvailable,
        boolean sellAvailable,
        boolean apiTradeAvailable,
        BigDecimal minPriceIncrement,
        OffsetDateTime first1MinCandleDate,
        OffsetDateTime first1DayCandleDate
) {
}