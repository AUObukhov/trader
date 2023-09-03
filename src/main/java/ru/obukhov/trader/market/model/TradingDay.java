package ru.obukhov.trader.market.model;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
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