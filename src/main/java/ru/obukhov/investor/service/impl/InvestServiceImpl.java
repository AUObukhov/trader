package ru.obukhov.investor.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.service.ConnectionService;
import ru.obukhov.investor.service.InvestService;
import ru.obukhov.investor.service.MarketService;
import ru.obukhov.investor.web.model.GetCandlesRequest;
import ru.tinkoff.invest.openapi.models.market.Candle;

import java.util.List;

@Log
@Service
@RequiredArgsConstructor
public class InvestServiceImpl implements InvestService {

    private final ApplicationContext appContext;
    private final ConnectionService connectionService;

    @Override
    public List<Candle> getCandles(GetCandlesRequest request) {
        MarketService marketService = getMarketService(request);

        List<Candle> candles = marketService.getMarketCandles(request.getTicker(),
                request.getTickerType(),
                request.getFrom(),
                request.getTo(),
                request.getCandleInterval());

        marketService.closeConnection();

        return candles;
    }

    @NotNull
    private MarketService getMarketService(GetCandlesRequest request) {
        return appContext.getBean(MarketService.class, connectionService, request.getToken());
    }

}