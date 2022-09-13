package ru.obukhov.trader.market.model;

import lombok.Builder;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record Etf(
        String figi,
        String ticker,
        Integer lotSize,
        Currency currency,
        String name,
        OffsetDateTime releasedDate,
        BigDecimal numShares,
        String country,
        Sector sector,
        SecurityTradingStatus tradingStatus,
        boolean buyAvailable,
        boolean sellAvailable,
        boolean apiTradeAvailable,
        BigDecimal minPriceIncrement,
        OffsetDateTime first1MinCandleDate,
        OffsetDateTime first1DayCandleDate
) {
}

