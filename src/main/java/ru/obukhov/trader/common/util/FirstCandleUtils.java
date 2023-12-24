package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.time.OffsetDateTime;
import java.time.Period;

/**
 * Utils do calculate first candles date based on known other first candles dates or adjust inaccurate candles date
 */
@UtilityClass
public class FirstCandleUtils {

    /**
     * According to statistics the oldest 1 day and older candles exist on this year
     */
    static final OffsetDateTime LONG_CANDLES_START_DATE_TIME = OffsetDateTime.of(1984, 1, 1, 0, 0, 0, 0, DateUtils.DEFAULT_OFFSET);

    /**
     * According to statistics the oldest candles smaller than 1 day exists 6 years ago
     */
    static final Period SHORT_CANDLES_PERIOD = Period.ofYears(6);

    public static OffsetDateTime getFirstCandleDate(
            final OffsetDateTime first1MinCandleDate,
            final OffsetDateTime first1DayCandleDate,
            final CandleInterval candleInterval
    ) {
        return switch (candleInterval) {
            case CANDLE_INTERVAL_1_MIN -> getMinimum1MinCandleDate(first1MinCandleDate);
            case CANDLE_INTERVAL_2_MIN -> getMinimum2MinCandleDate(first1MinCandleDate);
            case CANDLE_INTERVAL_3_MIN -> getMinimum3MinCandleDate(first1MinCandleDate);
            case CANDLE_INTERVAL_5_MIN -> getMinimum5MinCandleDate(first1MinCandleDate);
            case CANDLE_INTERVAL_10_MIN -> getMinimum10MinCandleDate(first1MinCandleDate);
            case CANDLE_INTERVAL_15_MIN -> getMinimum15MinCandleDate(first1MinCandleDate);
            case CANDLE_INTERVAL_30_MIN -> getMinimum30MinCandleDate(first1MinCandleDate);
            case CANDLE_INTERVAL_HOUR -> getMinimumHourCandleDate(first1MinCandleDate);
            case CANDLE_INTERVAL_2_HOUR -> getMinimum2HourCandleDate(first1MinCandleDate);
            case CANDLE_INTERVAL_4_HOUR -> getMinimum4HourCandleDate(first1MinCandleDate);
            case CANDLE_INTERVAL_DAY -> getMinimum1DayCandleDate(first1DayCandleDate);
            case CANDLE_INTERVAL_WEEK -> getMinimumWeekCandleDate(first1DayCandleDate);
            case CANDLE_INTERVAL_MONTH -> getMinimumMonthCandleDate(first1DayCandleDate);

            case CANDLE_INTERVAL_UNSPECIFIED,
                    UNRECOGNIZED -> throw new IllegalArgumentException("Unsupported CandleInterval " + candleInterval);
        };
    }

    private static OffsetDateTime getMinimum1MinCandleDate(final OffsetDateTime first1MinCandleDate) {
        return first1MinCandleDate == null ? getShortCandlesStartTime() : first1MinCandleDate;
    }

    private static OffsetDateTime getMinimum2MinCandleDate(final OffsetDateTime first1MinCandleDate) {
        return first1MinCandleDate == null ? getShortCandlesStartTime() : first1MinCandleDate.minusMinutes(1);
    }

    private static OffsetDateTime getMinimum3MinCandleDate(final OffsetDateTime first1MinCandleDate) {
        return first1MinCandleDate == null ? getShortCandlesStartTime() : first1MinCandleDate.minusMinutes(2);
    }

    private static OffsetDateTime getMinimum5MinCandleDate(final OffsetDateTime first1MinCandleDate) {
        return first1MinCandleDate == null ? getShortCandlesStartTime() : first1MinCandleDate.minusMinutes(4);
    }

    private static OffsetDateTime getMinimum10MinCandleDate(final OffsetDateTime first1MinCandleDate) {
        return first1MinCandleDate == null ? getShortCandlesStartTime() : first1MinCandleDate.minusMinutes(9);
    }

    private static OffsetDateTime getMinimum15MinCandleDate(final OffsetDateTime first1MinCandleDate) {
        return first1MinCandleDate == null ? getShortCandlesStartTime() : first1MinCandleDate.minusMinutes(14);
    }

    private static OffsetDateTime getMinimum30MinCandleDate(final OffsetDateTime first1MinCandleDate) {
        return first1MinCandleDate == null ? getShortCandlesStartTime() : first1MinCandleDate.minusMinutes(29);
    }

    private static OffsetDateTime getMinimumHourCandleDate(final OffsetDateTime first1MinCandleDate) {
        return first1MinCandleDate == null ? getShortCandlesStartTime() : first1MinCandleDate.minusMinutes(59);
    }

    private static OffsetDateTime getMinimum2HourCandleDate(final OffsetDateTime first1MinCandleDate) {
        return first1MinCandleDate == null ? getShortCandlesStartTime() : first1MinCandleDate.minusMinutes(299);
    }

    private static OffsetDateTime getMinimum4HourCandleDate(final OffsetDateTime first1MinCandleDate) {
        return first1MinCandleDate == null ? getShortCandlesStartTime() : first1MinCandleDate.minusMinutes(299);
    }

    private static OffsetDateTime getMinimum1DayCandleDate(final OffsetDateTime first1DayCandleDate) {
        return first1DayCandleDate == null ? LONG_CANDLES_START_DATE_TIME : first1DayCandleDate.minusHours(3);
    }

    private static OffsetDateTime getMinimumWeekCandleDate(final OffsetDateTime first1DayCandleDate) {
        return first1DayCandleDate == null
                ? LONG_CANDLES_START_DATE_TIME
                : DateUtils.toStartOfWeek(getMinimum1DayCandleDate(first1DayCandleDate));
    }

    private static OffsetDateTime getMinimumMonthCandleDate(final OffsetDateTime first1DayCandleDate) {
        return first1DayCandleDate == null ?
                LONG_CANDLES_START_DATE_TIME
                : DateUtils.toStartOfMonth(getMinimum1DayCandleDate(first1DayCandleDate));
    }

    private static OffsetDateTime getShortCandlesStartTime() {
        return DateUtils.now().minus(SHORT_CANDLES_PERIOD);
    }

}
