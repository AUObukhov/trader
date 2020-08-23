package ru.obukhov.investor.service;

import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.web.model.GetCandlesRequest;
import ru.obukhov.investor.web.model.GetSaldosRequest;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface InvestService {

    List<Candle> getCandles(GetCandlesRequest request);

    Map<LocalTime, BigDecimal> getSaldos(GetSaldosRequest request);
}