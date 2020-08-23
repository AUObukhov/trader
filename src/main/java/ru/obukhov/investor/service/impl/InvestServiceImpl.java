package ru.obukhov.investor.service.impl;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.exception.InvestorException;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.service.ConnectionService;
import ru.obukhov.investor.service.InvestService;
import ru.obukhov.investor.service.MarketService;
import ru.obukhov.investor.util.DateUtils;
import ru.obukhov.investor.util.MathUtils;
import ru.obukhov.investor.web.model.GetCandlesRequest;
import ru.obukhov.investor.web.model.GetStatisticsRequest;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
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

        List<Candle> candles = marketService.getCandles(request.getTicker(),
                request.getFrom(),
                request.getTo(),
                request.getCandleInterval());

        marketService.closeConnection();

        return candles;
    }

    @Override
    public Map<LocalTime, BigDecimal> getStatistics(GetStatisticsRequest request) {
        OffsetDateTime from = DateUtils.adjustFrom(request.getFrom());
        OffsetDateTime to = DateUtils.adjustTo(request.getTo());

        if (Duration.between(from, to).toDays() < 1) {
            throw new InvestorException("Date 'to' must be at least 1 day later than date 'from'");
        }

        MarketService marketService = getMarketService(request.getToken());
        List<Candle> candles = marketService.getCandles(request.getTicker(), from, to, CandleInterval.ONE_MIN);

        Multimap<LocalTime, BigDecimal> saldosByTimes = MultimapBuilder.treeKeys().linkedListValues().build();
        for (Candle candle : candles) {
            saldosByTimes.put(candle.getTime().toLocalTime(), candle.getSaldo());
        }

        return reduceMultimap(saldosByTimes, MathUtils::getAverage);

    }

    @NotNull
    private MarketService getMarketService(String token) {
        return appContext.getBean(MarketService.class, connectionService, token);
    }

}