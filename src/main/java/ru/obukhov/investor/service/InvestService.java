package ru.obukhov.investor.service;

import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.web.model.GetSaldosRequest;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public interface InvestService {

    List<Candle> getCandles(String token,
                            String ticker,
                            OffsetDateTime from,
                            OffsetDateTime to,
                            CandleInterval candleInterval);

    Map<LocalTime, BigDecimal> getSaldos(GetSaldosRequest request);
}