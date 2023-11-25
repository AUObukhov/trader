package ru.obukhov.trader.market.model;

import lombok.Builder;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.DateUtils;

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

    public TradingDay intersect(final TradingDay other) {
        final OffsetDateTime startTime = DateUtils.getLatestDateTime(this.startTime, other.startTime);
        final OffsetDateTime endTime = DateUtils.getEarliestDateTime(this.endTime, other.endTime);
        Assert.isTrue(startTime.isBefore(endTime), "Trading intervals are not intersecting");

        final OffsetDateTime openingAuctionStartTime = DateUtils.getLatestDateTime(this.openingAuctionStartTime, other.openingAuctionStartTime);
        final OffsetDateTime closingAuctionEndTime = DateUtils.getEarliestDateTime(this.closingAuctionEndTime, other.closingAuctionEndTime);
        validateBefore(openingAuctionStartTime, closingAuctionEndTime, "Auction intervals are not intersecting");

        final OffsetDateTime eveningOpeningAuctionStartTime = DateUtils.getLatestDateTime(this.eveningOpeningAuctionStartTime, other.eveningOpeningAuctionStartTime);

        final OffsetDateTime eveningStartTime = DateUtils.getLatestDateTime(this.eveningStartTime, other.eveningStartTime);
        final OffsetDateTime eveningEndTime = DateUtils.getEarliestDateTime(this.eveningEndTime, other.eveningEndTime);
        validateBefore(eveningStartTime, eveningEndTime, "Evening intervals are not intersecting");

        final OffsetDateTime clearingStartTime = DateUtils.getLatestDateTime(this.clearingStartTime, other.clearingStartTime);
        final OffsetDateTime clearingEndTime = DateUtils.getEarliestDateTime(this.clearingEndTime, other.clearingEndTime);
        validateBefore(clearingStartTime, clearingEndTime, "Clearing intervals are not intersecting");

        final OffsetDateTime premarketStartTime = DateUtils.getLatestDateTime(this.premarketStartTime, other.premarketStartTime);
        final OffsetDateTime premarketEndTime = DateUtils.getEarliestDateTime(this.premarketEndTime, other.premarketEndTime);
        validateBefore(premarketStartTime, premarketEndTime, "Premarket intervals are not intersecting");

        return new TradingDay(
                this.date,
                this.isTradingDay && other.isTradingDay,
                startTime,
                endTime,
                openingAuctionStartTime,
                closingAuctionEndTime,
                eveningOpeningAuctionStartTime,
                eveningStartTime,
                eveningEndTime,
                clearingStartTime,
                clearingEndTime,
                premarketStartTime,
                premarketEndTime
        );
    }

    private static void validateBefore(final OffsetDateTime from, final OffsetDateTime to, final String message) {
        if (from == null) {
            Assert.isTrue(to == null, message);
        } else {
            Assert.isTrue(to != null && from.isBefore(to), message);
        }
    }

}