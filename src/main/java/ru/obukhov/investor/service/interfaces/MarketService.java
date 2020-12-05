package ru.obukhov.investor.service.interfaces;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.Nullable;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.TickerType;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import java.time.OffsetDateTime;
import java.util.List;

public interface MarketService {

    @Cacheable("candles")
    List<Candle> getCandlesByFigi(String figi,
                                  @Nullable OffsetDateTime from,
                                  @Nullable OffsetDateTime to,
                                  CandleInterval interval);

    List<Candle> getCandlesByTicker(String ticker,
                                    @Nullable OffsetDateTime from,
                                    @Nullable OffsetDateTime to,
                                    CandleInterval interval);

    Candle getLastCandleByTicker(String ticker);

    Candle getLastCandleByFigi(String figi);

    Candle getLastCandleByTicker(String ticker, OffsetDateTime to);

    Candle getLastCandleByFigi(String ticker, OffsetDateTime to);

    @Cacheable("instrument")
    Instrument getInstrument(String ticker);

    List<Instrument> getInstruments(TickerType type);

    String getFigi(String ticker);
}