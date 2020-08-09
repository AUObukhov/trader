package ru.obukhov.investor.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.service.ConnectionService;
import ru.obukhov.investor.service.InvestService;
import ru.obukhov.investor.service.MarketService;
import ru.obukhov.investor.web.model.GetCandlesRequest;
import ru.obukhov.investor.web.model.GetStatisticsRequest;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Log
@Service
@RequiredArgsConstructor
public class InvestServiceImpl implements InvestService {

    private final ApplicationContext appContext;
    private final ConnectionService connectionService;

    @Override
    public List<Candle> getCandles(GetCandlesRequest request) {
        MarketService marketService = getMarketService(request.getToken());

        List<Candle> candles = marketService.getMarketCandles(request.getTicker(),
                request.getTickerType(),
                request.getFrom(),
                request.getTo(),
                request.getCandleInterval());

        marketService.closeConnection();

        return candles;
    }

    @Override
    public void getStatistics(GetStatisticsRequest request) {
        MarketService marketService = getMarketService(request.getToken());
        OffsetDateTime to = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS);
        OffsetDateTime from = to.minusDays(1);

        List<List<Candle>> candlesByDays = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            List<Candle> candles = marketService.getMarketCandles(request.getTicker(),
                    request.getTickerType(),
                    from,
                    to,
                    CandleInterval.ONE_MIN);
            candlesByDays.add(candles);

            to = from;
            from = to.minusDays(1);
        }

    }

    @NotNull
    private MarketService getMarketService(String token) {
        return appContext.getBean(MarketService.class, connectionService, token);
    }

}