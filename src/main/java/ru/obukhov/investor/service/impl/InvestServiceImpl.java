package ru.obukhov.investor.service.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
        OffsetDateTime to = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS);
        OffsetDateTime from = to.minusDays(1);

        List<Candle> candles = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            List<Candle> currentCandles = marketService.getMarketCandles(request.getTicker(),
                    request.getTickerType(),
                    from,
                    to,
                    CandleInterval.ONE_MIN);
            candles.addAll(currentCandles);

            to = from;
            from = to.minusDays(1);
        }

        Multimap<LocalTime, BigDecimal> saldosByTimes = ArrayListMultimap.create();

        for (Candle candle : candles) {
            saldosByTimes.put(candle.getTime().toLocalTime(), candle.getSaldo());
        }

        return saldosByTimes.asMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> getAverage(e.getValue()),
                        (x1, x2) -> null,
                        TreeMap::new));

    }

    @NotNull
    private BigDecimal getAverage(Collection<BigDecimal> saldos) {
        double averageDouble = saldos.stream()
                .mapToInt(BigDecimal::intValue)
                .average()
                .orElse(0);
        return BigDecimal.valueOf(averageDouble).setScale(2, RoundingMode.HALF_UP);
    }

    @NotNull
    private MarketService getMarketService(String token) {
        return appContext.getBean(MarketService.class, connectionService, token);
    }

}