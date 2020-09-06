package ru.obukhov.investor.service.impl;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.TickerType;
import ru.obukhov.investor.service.InvestService;
import ru.obukhov.investor.service.MarketService;
import ru.obukhov.investor.util.DateUtils;
import ru.obukhov.investor.util.MathUtils;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static ru.obukhov.investor.util.CollectionUtils.reduceMultimap;

@Log
@Service
@RequiredArgsConstructor
public class InvestServiceImpl implements InvestService {

    private final ApplicationContext appContext;
    private final MarketService marketService;

    /**
     * Searches candles by conditions
     *
     * @param ticker         ticker of candles
     * @param from           beginning of search interval, {@link DateUtils#START_DATE} if null
     * @param to             end of search interval, current date and time if null
     * @param candleInterval candle interval
     * @return list of found candles
     */
    @Override
    public List<Candle> getCandles(String ticker,
                                   OffsetDateTime from,
                                   OffsetDateTime to,
                                   CandleInterval candleInterval) {

        OffsetDateTime adjustedFrom = DateUtils.getDefaultFromIfNull(from);
        OffsetDateTime adjustedTo = DateUtils.getDefaultToIfNull(to);
        TemporalUnit periodUnit = DateUtils.getPeriodUnitByCandleInterval(candleInterval);

        return marketService.getCandles(ticker, adjustedFrom, adjustedTo, candleInterval, periodUnit);
    }

    /**
     * Searches saldos by conditions and groups then by time
     *
     * @param ticker         ticker of candles
     * @param from           beginning of search interval, {@link DateUtils#START_DATE} if null
     * @param to             end of search interval, current date and time if null
     * @param candleInterval candle interval, allowed values:
     *                       ONE_MIN, TWO_MIN, THREE_MIN, FIVE_MIN, TEN_MIN, QUARTER_HOUR, HALF_HOUR,
     *                       HOUR, TWO_HOURS, FOUR_HOURS
     * @return {@link Map} time to saldo
     */
    @Override
    public Map<LocalTime, BigDecimal> getDailySaldos(String ticker,
                                                     OffsetDateTime from,
                                                     OffsetDateTime to,
                                                     CandleInterval candleInterval) {

        List<Candle> candles = getCandles(ticker, from, to, candleInterval);

        Multimap<LocalTime, BigDecimal> saldosByTimes = MultimapBuilder.treeKeys().linkedListValues().build();
        for (Candle candle : candles) {
            saldosByTimes.put(candle.getTime().toLocalTime(), candle.getSaldo());
        }

        return new TreeMap<>(reduceMultimap(saldosByTimes, MathUtils::getAverageMoney));

    }

    /**
     * Searches saldos by conditions and groups them by day of week
     *
     * @param ticker ticker of candles
     * @param from   beginning of search interval, {@link DateUtils#START_DATE} if null
     * @param to     end of search interval, current date and time if null
     * @return {@link Map} day of week to saldo
     */
    @Override
    public Map<DayOfWeek, BigDecimal> getWeeklySaldos(String ticker, OffsetDateTime from, OffsetDateTime to) {

        List<Candle> candles = getCandles(ticker, from, to, CandleInterval.DAY);

        Multimap<DayOfWeek, BigDecimal> saldosByDaysOfWeek = MultimapBuilder.treeKeys().linkedListValues().build();
        for (Candle candle : candles) {
            saldosByDaysOfWeek.put(candle.getTime().getDayOfWeek(), candle.getSaldo());
        }

        return new TreeMap<>(reduceMultimap(saldosByDaysOfWeek, MathUtils::getAverageMoney));

    }

    /**
     * @param type nullable ticker type
     * @return all instruments of given {@code type}, or all instruments at all if {@code type} is null
     */
    @Override
    public List<Instrument> getInstruments(@Nullable TickerType type) {
        return marketService.getInstruments(type);
    }

}