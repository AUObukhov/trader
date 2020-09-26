package ru.obukhov.investor.service.interfaces;

import org.springframework.lang.Nullable;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.TickerType;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import java.time.OffsetDateTime;
import java.util.List;

public interface MarketService {

    List<Candle> getCandles(String ticker,
                            @Nullable OffsetDateTime from,
                            @Nullable OffsetDateTime to,
                            CandleInterval interval);

    Candle getLastCandle(String ticker);

    List<Instrument> getInstruments(TickerType type);

    String getFigi(String ticker);
}