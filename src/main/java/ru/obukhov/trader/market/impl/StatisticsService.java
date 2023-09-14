package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.impl.MovingAverager;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.market.model.SetCapitalization;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service to get extended statistics about market prices and instruments
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private static final int ORDER = 1;

    private final ExtMarketDataService extMarketDataService;
    private final ExtInstrumentsService extInstrumentsService;
    private final ApplicationContext applicationContext;

    /**
     * Searches candles by conditions and calculates extra data by them
     *
     * @param figi           Financial Instrument Global Identifier
     * @param interval       search interval, default {@code interval.from} is start of trading, default {@code interval.to} is now
     * @param candleInterval candle interval
     * @return data structure with list of found candles and extra data
     */
    public GetCandlesResponse getExtendedCandles(
            final String figi,
            final Interval interval,
            final CandleInterval candleInterval,
            final MovingAverageType movingAverageType,
            final int smallWindow,
            final int bigWindow
    ) {
        final Interval innerInterval = Interval.of(interval.getFrom(), ObjectUtils.defaultIfNull(interval.getTo(), OffsetDateTime.now()));
        final List<Candle> candles = extMarketDataService.getCandles(figi, innerInterval, candleInterval);

        final MovingAverager averager = applicationContext.getBean(movingAverageType.getAveragerName(), MovingAverager.class);
        final List<BigDecimal> opens = candles.stream().map(Candle::getOpen).toList();
        final List<BigDecimal> shortAverages = averager.getAverages(opens, smallWindow, ORDER);
        final List<BigDecimal> longAverages = averager.getAverages(opens, bigWindow, ORDER);

        return new GetCandlesResponse(candles, shortAverages, longAverages);
    }

    /**
     * Calculates weights of shares in set.
     * Weight is (capitalization of the share) / (sum of capitalizations of all shares in the set).
     *
     * @param shareFigies list of set shares FIGIes
     * @return map with given {@code shareFigies} as keys and their weights as values
     */
    public Map<String, BigDecimal> getCapitalizationWeights(final List<String> shareFigies) {
        final SetCapitalization capitalization = getCapitalization(shareFigies);
        return capitalization.sharesCapitalizations().keySet().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        figi -> getWeight(figi, capitalization),
                        (x1, x2) -> {
                            throw new IllegalStateException("Unexpected merge");
                        },
                        LinkedHashMap::new
                ));
    }

    private BigDecimal getWeight(final String figi, final SetCapitalization capitalization) {
        return DecimalUtils.divide(capitalization.sharesCapitalizations().get(figi), capitalization.totalCapitalization());
    }

    /**
     * Calculates capitalizations of shares in set.
     *
     * @param shareFigies list of set shares FIGIes
     */
    public SetCapitalization getCapitalization(final List<String> shareFigies) {
        final List<Share> shares = extInstrumentsService.getShares(shareFigies);
        final Map<String, BigDecimal> prices = extMarketDataService.getLastPrices(shareFigies);
        final Map<String, BigDecimal> securitiesCapitalizations = new LinkedHashMap<>(shareFigies.size(), 1);
        BigDecimal totalCapitalization = BigDecimal.ZERO;
        for (final Share share : shares) {
            final BigDecimal nominalPrice = prices.get(share.figi());
            final BigDecimal rubPrice = extMarketDataService.convertCurrency(share.currency(), Currencies.RUB, nominalPrice);
            final BigDecimal capitalization = DecimalUtils.multiply(rubPrice, share.issueSize());
            securitiesCapitalizations.put(share.figi(), capitalization);
            totalCapitalization = totalCapitalization.add(capitalization);
        }

        return new SetCapitalization(securitiesCapitalizations, totalCapitalization);
    }

}