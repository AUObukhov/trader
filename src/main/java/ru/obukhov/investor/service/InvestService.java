package ru.obukhov.investor.service;

import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.web.model.GetCandlesRequest;
import ru.obukhov.investor.web.model.GetStatisticsRequest;

import java.util.List;

public interface InvestService {

    List<Candle> getCandles(GetCandlesRequest request);

    void getStatistics(GetStatisticsRequest request);
}