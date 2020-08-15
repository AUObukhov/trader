package ru.obukhov.investor.service.impl;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.TickerType;
import ru.obukhov.investor.service.ConnectionService;
import ru.obukhov.investor.service.InvestService;
import ru.obukhov.investor.service.MarketService;
import ru.obukhov.investor.util.MathUtils;
import ru.obukhov.investor.web.model.GetCandlesRequest;
import ru.obukhov.investor.web.model.GetStatisticsRequest;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.obukhov.investor.util.CollectionUtils.reduceMultimap;

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
    public Map<LocalTime, BigDecimal> getStatistics(GetStatisticsRequest request) {
        MarketService marketService = getMarketService(request.getToken());
        List<Candle> candles = getCandles(request.getTicker(),
                request.getTickerType(),
                request.getFrom(),
                request.getTo(),
                CandleInterval.ONE_MIN,
                marketService);

        Multimap<LocalTime, BigDecimal> saldosByTimes = MultimapBuilder.treeKeys().linkedListValues().build();
        for (Candle candle : candles) {
            saldosByTimes.put(candle.getTime().toLocalTime(), candle.getSaldo());
        }

        return reduceMultimap(saldosByTimes, MathUtils::getAverage);

    }

    @NotNull
    private List<Candle> getCandles(String ticker,
                                    TickerType tickerType,
                                    OffsetDateTime from,
                                    OffsetDateTime to,
                                    CandleInterval interval,
                                    MarketService marketService) {
        OffsetDateTime currentFrom = from;
        OffsetDateTime currentTo = currentFrom.plusDays(1);

        List<Candle> candles = new ArrayList<>();
        while (currentFrom.isBefore(to)) {
            List<Candle> currentCandles = marketService.getMarketCandles(ticker,
                    tickerType,
                    currentFrom,
                    currentTo,
                    interval);
            candles.addAll(currentCandles);

            currentFrom = currentTo;
            currentTo = currentFrom.plusDays(1);
        }

        return candles;
    }

    @NotNull
    private MarketService getMarketService(String token) {
        return appContext.getBean(MarketService.class, connectionService, token);
    }

}