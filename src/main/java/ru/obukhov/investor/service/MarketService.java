package ru.obukhov.investor.service;

import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.TickerType;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalUnit;
import java.util.List;

public interface MarketService {

    List<Candle> getCandles(String ticker,
                            OffsetDateTime from,
                            OffsetDateTime to,
                            CandleInterval interval,
                            TemporalUnit periodUnit);

    List<Instrument> getInstruments(TickerType type);

    void closeConnection();
}