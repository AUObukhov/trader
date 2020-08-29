package ru.obukhov.investor.service.impl;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.service.ConnectionService;
import ru.obukhov.investor.service.InvestService;
import ru.obukhov.investor.service.MarketService;
import ru.obukhov.investor.util.DateUtils;
import ru.obukhov.investor.util.MathUtils;
import ru.obukhov.investor.web.model.GetSaldosRequest;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static ru.obukhov.investor.util.CollectionUtils.reduceMultimap;

@Log
@Service
@RequiredArgsConstructor
public class InvestServiceImpl implements InvestService {

    private final ApplicationContext appContext;
    private final ConnectionService connectionService;

    @Override
    public List<Candle> getCandles(String token,
                                   String ticker,
                                   OffsetDateTime from,
                                   OffsetDateTime to,
                                   CandleInterval candleInterval) {

        OffsetDateTime adjustedFrom = DateUtils.getDefaultFromIfNull(from);
        OffsetDateTime adjustedTo = DateUtils.getDefaultToIfNull(to);

        MarketService marketService = getMarketService(token);

        List<Candle> candles = marketService.getCandles(ticker, adjustedFrom, adjustedTo, candleInterval);

        marketService.closeConnection();

        return candles;
    }

    @Override
    public Map<LocalTime, BigDecimal> getSaldos(GetSaldosRequest request) {
        List<Candle> candles = getCandles(request.getToken(),
                request.getTicker(),
                request.getFrom(),
                request.getTo(),
                request.getCandleInterval());

        Multimap<LocalTime, BigDecimal> saldosByTimes = MultimapBuilder.treeKeys().linkedListValues().build();
        for (Candle candle : candles) {
            saldosByTimes.put(candle.getTime().toLocalTime(), candle.getSaldo());
        }

        return new TreeMap<>(reduceMultimap(saldosByTimes, MathUtils::getAverageMoney));

    }

    @NotNull
    private MarketService getMarketService(String token) {
        return appContext.getBean(MarketService.class, connectionService, token);
    }

}