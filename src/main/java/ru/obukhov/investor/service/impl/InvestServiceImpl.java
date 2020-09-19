package ru.obukhov.investor.service.impl;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import static ru.obukhov.investor.util.CollectionUtils.reduceMultimap;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestServiceImpl implements InvestService {

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

        return marketService.getCandles(ticker, from, to, candleInterval);
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
     * @return Map time to saldo
     */
    @Override
    public Map<Object, BigDecimal> getDailySaldos(String ticker,
                                                  OffsetDateTime from,
                                                  OffsetDateTime to,
                                                  CandleInterval candleInterval) {

        return getSaldos(ticker, from, to, candleInterval, OffsetDateTime::toLocalTime);
    }

    /**
     * Searches saldos by conditions and groups them by day of week
     *
     * @param ticker ticker of candles
     * @param from   beginning of search interval, {@link DateUtils#START_DATE} if null
     * @param to     end of search interval, current date and time if null
     * @return Map day of week to saldo
     */
    @Override
    public Map<Object, BigDecimal> getWeeklySaldos(String ticker, OffsetDateTime from, OffsetDateTime to) {

        return getSaldos(ticker, from, to, CandleInterval.DAY, OffsetDateTime::getDayOfWeek);

    }

    /**
     * Searches saldos by conditions and groups them by day of month
     *
     * @param ticker ticker of candles
     * @param from   beginning of search interval, {@link DateUtils#START_DATE} if null
     * @param to     end of search interval, current date and time if null
     * @return Map day of month to saldo
     */
    @Override
    public Map<Object, BigDecimal> getMonthlySaldos(String ticker, OffsetDateTime from, OffsetDateTime to) {

        return getSaldos(ticker, from, to, CandleInterval.DAY, OffsetDateTime::getDayOfMonth);

    }

    @Override
    public Map<Object, BigDecimal> getYearlySaldos(String ticker, OffsetDateTime from, OffsetDateTime to) {

        return getSaldos(ticker, from, to, CandleInterval.MONTH, OffsetDateTime::getMonth);

    }

    /**
     * Searches saldos by conditions and groups them by time unit provided by {@code keyExtractor}
     *
     * @param ticker         ticker of candles
     * @param from           beginning of search interval, {@link DateUtils#START_DATE} if null
     * @param to             end of search interval, current date and time if null
     * @param candleInterval candle interval
     * @param keyExtractor   function getting of Map key from {@code OffsetDateTime}
     * @return ordered Map with saldos as values
     */
    private Map<Object, BigDecimal> getSaldos(String ticker,
                                              OffsetDateTime from,
                                              OffsetDateTime to,
                                              CandleInterval candleInterval,
                                              Function<OffsetDateTime, Object> keyExtractor) {

        List<Candle> candles = getCandles(ticker, from, to, candleInterval);

        Multimap<Object, BigDecimal> saldosByTimes = (Multimap) MultimapBuilder.treeKeys().linkedListValues().build();
        for (Candle candle : candles) {
            saldosByTimes.put(keyExtractor.apply(candle.getTime()), candle.getSaldo());
        }

        return new TreeMap<>(reduceMultimap(saldosByTimes, MathUtils::getAverageMoney));

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