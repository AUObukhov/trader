package ru.obukhov.trader.common.model;

import lombok.experimental.UtilityClass;
import ru.obukhov.trader.common.util.DateUtils;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.time.OffsetDateTime;
import java.time.Period;

@UtilityClass
public class Periods {

    public static final Period DAY = Period.ofDays(1);
    public static final Period TWO_DAYS = Period.ofDays(2);
    public static final Period WEEK = Period.ofWeeks(1);
    public static final Period MONTH = Period.ofMonths(1);
    public static final Period YEAR = Period.ofYears(1);
    public static final Period TWO_YEARS = Period.ofYears(2);
    public static final Period DECADE = Period.ofYears(10);

    /**
     * @return maximum period to load candles.
     * @see <a href=https://tinkoff.github.io/investAPI/faq_marketdata/#_20>documentation</a>
     */
    public static Period getPeriodByCandleInterval(final CandleInterval candleInterval) {
        return switch (candleInterval) {
            case CANDLE_INTERVAL_1_MIN,
                    CANDLE_INTERVAL_2_MIN,
                    CANDLE_INTERVAL_3_MIN,
                    CANDLE_INTERVAL_5_MIN,
                    CANDLE_INTERVAL_10_MIN,
                    CANDLE_INTERVAL_15_MIN -> Period.ofDays(1);

            case CANDLE_INTERVAL_30_MIN -> Period.ofDays(2);
            case CANDLE_INTERVAL_HOUR -> Period.ofWeeks(1);

            case CANDLE_INTERVAL_2_HOUR,
                    CANDLE_INTERVAL_4_HOUR -> Period.ofMonths(1);

            case CANDLE_INTERVAL_DAY -> Period.ofYears(1);
            case CANDLE_INTERVAL_WEEK -> Period.ofYears(2);
            case CANDLE_INTERVAL_MONTH -> Period.ofYears(10);

            case CANDLE_INTERVAL_UNSPECIFIED,
                    UNRECOGNIZED -> throw new IllegalArgumentException("Unsupported CandleInterval " + candleInterval);
        };
    }

    /**
     * @param dateTime must be positive, otherwise the result may be wrong.
     * @return dateTime in the beginning of given {@code period} containing given {@code dateTime}
     */
    public static OffsetDateTime toStartOfPeriod(final OffsetDateTime dateTime, final Period period) {
        if (period.equals(DAY)) {
            return DateUtils.toStartOfDay(dateTime);
        } else if (period.equals(TWO_DAYS)) {
            return DateUtils.toStartOf2Days(dateTime);
        } else if (period.equals(WEEK)) {
            return DateUtils.toStartOfWeek(dateTime);
        } else if (period.equals(MONTH)) {
            return DateUtils.toStartOfMonth(dateTime);
        } else if (period.equals(YEAR)) {
            return DateUtils.toStartOfYear(dateTime);
        } else if (period.equals(TWO_YEARS)) {
            return DateUtils.toStartOf2Years(dateTime);
        } else if (period.equals(DECADE)) {
            return DateUtils.toStartOfDecade(dateTime);
        } else {
            throw new IllegalArgumentException("Unsupported period " + period);
        }
    }

    /**
     * @param dateTime must be positive, otherwise the result may be wrong.
     * @return dateTime in the end of given {@code period} containing given {@code dateTime}
     */
    public static OffsetDateTime toEndOfPeriod(final OffsetDateTime dateTime, final Period period) {
        if (period.equals(DAY)) {
            return DateUtils.toEndOfDay(dateTime);
        } else if (period.equals(TWO_DAYS)) {
            return DateUtils.toEndOf2Days(dateTime);
        } else if (period.equals(WEEK)) {
            return DateUtils.toEndOfWeek(dateTime);
        } else if (period.equals(MONTH)) {
            return DateUtils.toEndOfMonth(dateTime);
        } else if (period.equals(YEAR)) {
            return DateUtils.toEndOfYear(dateTime);
        } else if (period.equals(TWO_YEARS)) {
            return DateUtils.toEndOf2Years(dateTime);
        } else if (period.equals(DECADE)) {
            return DateUtils.toEndOfDecade(dateTime);
        } else {
            throw new IllegalArgumentException("Unsupported period " + period);
        }
    }

}