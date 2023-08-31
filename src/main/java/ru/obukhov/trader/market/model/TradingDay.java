package ru.obukhov.trader.market.model;

import java.time.OffsetDateTime;

public record TradingDay(
        OffsetDateTime date,
        boolean isTradingDay,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        OffsetDateTime openingAuctionStartTime,
        OffsetDateTime closingAuctionEndTime,
        OffsetDateTime eveningOpeningAuctionStartTime,
        OffsetDateTime eveningStartTime,
        OffsetDateTime eveningEndTime,
        OffsetDateTime clearingStartTime,
        OffsetDateTime clearingEndTime,
        OffsetDateTime premarketStartTime,
        OffsetDateTime premarketEndTime
) {
}