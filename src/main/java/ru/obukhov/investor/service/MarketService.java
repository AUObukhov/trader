package ru.obukhov.investor.service;

import org.jetbrains.annotations.NotNull;
import ru.obukhov.investor.model.TickerType;
import ru.tinkoff.invest.openapi.models.market.Candle;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import java.time.OffsetDateTime;
import java.util.List;

public interface MarketService {

    List<Instrument> getInstruments(TickerType type);

    List<Candle> getMarketCandles(@NotNull String ticker,
                                  @NotNull TickerType type,
                                  @NotNull OffsetDateTime from,
                                  @NotNull OffsetDateTime to,
                                  @NotNull CandleInterval interval);

    void closeConnection();
}