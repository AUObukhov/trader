package ru.obukhov.investor.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import ru.obukhov.investor.common.model.Interval;
import ru.obukhov.investor.market.interfaces.MarketService;
import ru.obukhov.investor.market.interfaces.StatisticsService;
import ru.obukhov.investor.market.model.Candle;
import ru.obukhov.investor.market.model.TickerType;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final MarketService marketService;

    /**
     * Searches candles by conditions
     *
     * @param ticker         ticker of candles
     * @param interval       search interval, default interval.from is start of trading, default interval.to is now
     * @param candleInterval candle interval
     * @return list of found candles
     */
    @Override
    public List<Candle> getCandles(String ticker, Interval interval, CandleInterval candleInterval) {
        return marketService.getCandles(ticker, interval, candleInterval);
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