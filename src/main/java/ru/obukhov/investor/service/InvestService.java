package ru.obukhov.investor.service;

import ru.obukhov.investor.web.model.GetCandlesRequest;
import ru.tinkoff.invest.openapi.models.market.Candle;

import java.util.List;

public interface InvestService {

    List<Candle> getCandles(GetCandlesRequest request);
}