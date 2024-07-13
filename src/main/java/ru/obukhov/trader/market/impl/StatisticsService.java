package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.impl.MovingAverager;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.MapUtils;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Dividend;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.market.model.SetCapitalization;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.web.model.SharesFiltrationOptions;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.ShareType;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
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
    private final TradingProperties tradingProperties;

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
        final Interval innerInterval = Interval.of(interval.getFrom(), ObjectUtils.defaultIfNull(interval.getTo(), DateUtils.now()));
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
    public SequencedMap<String, BigDecimal> getCapitalizationWeights(final List<String> shareFigies) {
        final SetCapitalization capitalization = getCapitalization(shareFigies);
        return capitalization.sharesCapitalizations().keySet().stream()
                .collect(MapUtils.newSequencedMapValueCollector(figi -> getWeight(figi, capitalization)));
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

    public SequencedMap<String, Double> getMostProfitableShares(final SharesFiltrationOptions filtrationOptions) {
        List<Share> shares = extInstrumentsService.getAllShares();

        log.info("Found {} shares total", shares.size());

        final OffsetDateTime now = DateUtils.now();

        shares = filterByCurrencies(shares, filtrationOptions.currencies());
        shares = filterByApiTradeAvailableFlag(shares, filtrationOptions.apiTradeAvailableFlag());
        shares = filterByForQualInvestorFlag(shares, filtrationOptions.forQualInvestorFlag());
        shares = filterByForIisFlag(shares, filtrationOptions.forIisFlag());
        shares = filterByShareTypes(shares, filtrationOptions.shareTypes());
        shares = filterByMinTradingDays(shares, now, filtrationOptions.minTradingDays());

        final Map<String, List<Dividend>> dividends = getDividends(shares, now);

        shares = filterByHavingDividendsWithinDays(shares, dividends, now, filtrationOptions.havingDividendsWithinDays());

        final Map<String, Double> result = new HashMap<>();
        final Currency usdCurrency = getUsdCurrency();
        final double usdRegularInvestingAnnualReturn = getRegularInvestingAnnualReturn(usdCurrency, now);
        result.put(usdCurrency.name(), usdRegularInvestingAnnualReturn);

        final Map<String, Double> regularInvestingAnnualReturns = getRegularInvestingAnnualReturns(shares, dividends, now);
        shares = filterByRegularInvestingAnnualReturns(
                shares,
                regularInvestingAnnualReturns,
                usdRegularInvestingAnnualReturn,
                filtrationOptions.filterByRegularInvestingAnnualReturns()
        );

        for (final Share share : shares) {
            result.put(share.name(), regularInvestingAnnualReturns.get(share.figi()));
        }
        return MapUtils.sortByValue(result);
    }

    private static List<Share> filterByCurrencies(final List<Share> shares, @Nullable final List<String> currencies) {
        if (CollectionUtils.isEmpty(currencies)) {
            return shares;
        }

        final List<Share> result = shares.stream()
                .filter(share -> currencies.contains(share.currency()))
                .toList();
        log.info("Remaining {} shares after filtration by currencies {}", result.size(), currencies);
        return result;
    }

    private static List<Share> filterByApiTradeAvailableFlag(final List<Share> shares, @Nullable final Boolean apiTradeAvailableFlag) {
        if (apiTradeAvailableFlag == null) {
            return shares;
        }

        final List<Share> result = shares.stream()
                .filter(share -> share.apiTradeAvailableFlag() == apiTradeAvailableFlag)
                .toList();
        log.info("Remaining {} shares after filtration by apiTradeAvailableFlag = {}", result.size(), apiTradeAvailableFlag);
        return result;
    }

    private static List<Share> filterByForQualInvestorFlag(final List<Share> shares, @Nullable final Boolean forQualInvestorFlag) {
        if (forQualInvestorFlag == null) {
            return shares;
        }

        final List<Share> result = shares.stream()
                .filter(share -> share.forQualInvestorFlag() == forQualInvestorFlag)
                .toList();
        log.info("Remaining {} shares after filtration by forQualInvestorFlag = {}", result.size(), forQualInvestorFlag);
        return result;
    }

    private static List<Share> filterByForIisFlag(final List<Share> shares, @Nullable final Boolean forIisFlag) {
        if (forIisFlag == null) {
            return shares;
        }

        final List<Share> result = shares.stream()
                .filter(share -> share.forIisFlag() == forIisFlag)
                .toList();
        log.info("Remaining {} shares after filtration by forIisFlag = {}", result.size(), forIisFlag);
        return result;
    }

    private static List<Share> filterByShareTypes(final List<Share> shares, @Nullable List<ShareType> shareTypes) {
        if (CollectionUtils.isEmpty(shareTypes)) {
            return shares;
        }

        final List<Share> result = shares.stream()
                .filter(share -> shareTypes.contains(share.shareType()))
                .toList();
        log.info("Remaining {} shares after filtration by share types {}", result.size(), shareTypes);
        return result;
    }

    private static List<Share> filterByMinTradingDays(final List<Share> shares, final OffsetDateTime now, @Nullable final Integer minTradingDays) {
        if (minTradingDays == null) {
            return shares;
        }

        final List<Share> result = shares.stream()
                .filter(share -> intervalIsLonger(share.first1DayCandleDate(), now, minTradingDays))
                .toList();
        log.info("Remaining {} shares after filtration by trading for more than {} years", result.size(), minTradingDays);
        return result;
    }

    private static boolean intervalIsLonger(@Nullable final OffsetDateTime from, final OffsetDateTime to, final int minTradingDays) {
        return from != null && ChronoUnit.DAYS.between(from, to) > minTradingDays;
    }

    private Map<String, List<Dividend>> getDividends(final List<Share> shares, final OffsetDateTime now) {
        return shares.stream().collect(Collectors.toMap(Share::uid, share -> getDividends(share, now)));
    }

    private List<Dividend> getDividends(final Share share, final OffsetDateTime now) {
        final OffsetDateTime from = ObjectUtils.defaultIfNull(share.first1DayCandleDate(), tradingProperties.getTradesStart());
        final Interval interval = Interval.of(from, now);
        return extInstrumentsService.getDividends(share.figi(), interval);
    }

    private static List<Share> filterByHavingDividendsWithinDays(
            final List<Share> shares,
            final Map<String, List<Dividend>> dividends,
            final OffsetDateTime now,
            @Nullable final Integer havingDividendsWithinDays
    ) {
        if (havingDividendsWithinDays == null) {
            return shares;
        }

        final OffsetDateTime pivotDate = now.minusDays(havingDividendsWithinDays);
        final List<Share> result = shares.stream()
                .filter(share -> havingDividendsAfter(dividends.get(share.uid()), pivotDate))
                .toList();
        log.info("Remaining {} shares after filtration by having dividends within last {} days", result.size(), havingDividendsWithinDays);
        return result;
    }

    private static boolean havingDividendsAfter(final List<Dividend> dividends, final OffsetDateTime pivotDate) {
        return dividends.stream()
                .map(Dividend::declaredDate)
                .anyMatch(pivotDate::isBefore);
    }

    private Map<String, Double> getRegularInvestingAnnualReturns(
            final List<Share> shares,
            final Map<String, List<Dividend>> dividends,
            final OffsetDateTime now
    ) {
        final Map<String, BigDecimal> lastPrices = extMarketDataService.getSharesPrices(shares, now);
        final Function<Share, Double> regularInvestingAnnualReturnsFunction = share -> {
            final List<Dividend> shareDividends = dividends.get(share.uid());
            final BigDecimal lastPrice = lastPrices.get(share.figi());
            return getRegularInvestingAnnualReturn(share, shareDividends, lastPrice, now);
        };
        return shares.stream().collect(Collectors.toMap(Share::figi, regularInvestingAnnualReturnsFunction));
    }

    private List<Share> filterByRegularInvestingAnnualReturns(
            final List<Share> shares,
            final Map<String, Double> regularInvestingAnnualReturns,
            final double minimumReturn,
            final boolean filterByRegularInvestingAnnualReturns
    ) {
        if (!filterByRegularInvestingAnnualReturns) {
            return shares;
        }

        final List<Share> result = shares.stream()
                .filter(share -> regularInvestingAnnualReturns.get(share.figi()) > minimumReturn)
                .toList();
        log.info("Remaining {} shares after filtration by minimum regular investing annual return {}", result.size(), minimumReturn);

        return result;
    }

    private Currency getUsdCurrency() {
        return extInstrumentsService.getCurrenciesByIsoNames(Currencies.USD)
                .stream()
                .filter(share -> share.ticker().endsWith("TOM"))
                .findFirst()
                .orElseThrow();
    }

    private double getRegularInvestingAnnualReturn(final Currency currency, final OffsetDateTime to) {
        final OffsetDateTime from = tradingProperties.getTradesStart();
        final Interval interval = Interval.of(from, to);
        final List<Candle> candles = extMarketDataService.getCandles(currency.figi(), interval, CandleInterval.CANDLE_INTERVAL_MONTH);

        final BigDecimal expenses = candles.stream().map(Candle::getOpen).reduce(BigDecimal.ZERO, BigDecimal::add);
        final BigDecimal lastPrice = extMarketDataService.getPrice(currency, to);
        final BigDecimal absoluteReturn = DecimalUtils.multiply(lastPrice, candles.size());

        return getRelativeAnnualReturn(absoluteReturn, expenses, from, to);
    }

    private double getRegularInvestingAnnualReturn(
            final Share share,
            final List<Dividend> dividends,
            final BigDecimal lastPrice,
            final OffsetDateTime to
    ) {
        final String nominalCurrency = share.currency();
        final OffsetDateTime from = share.first1DayCandleDate();
        final Interval interval = Interval.of(from, to);
        final List<Candle> candles = extMarketDataService.getCandles(share.figi(), interval, CandleInterval.CANDLE_INTERVAL_MONTH);
        int shareCount = 0;
        BigDecimal expenses = DecimalUtils.ZERO;
        BigDecimal absoluteReturn = DecimalUtils.ZERO;
        int dividendsIndex = 0;
        for (final Candle candle : candles) {
            while (dividendsIndex < dividends.size() && dividends.get(dividendsIndex).isBefore(candle.getTime())) {
                absoluteReturn = absoluteReturn.add(getDividendRub(dividends.get(dividendsIndex), shareCount, nominalCurrency));
                dividendsIndex++;
            }
            final BigDecimal expense = extMarketDataService.convertCurrency(nominalCurrency, Currencies.RUB, candle.getOpen(), candle.getTime());
            expenses = expenses.add(expense);
            shareCount++;
        }

        while (dividendsIndex < dividends.size() && dividends.get(dividendsIndex).isBefore(to)) {
            absoluteReturn = absoluteReturn.add(getDividendRub(dividends.get(dividendsIndex), shareCount, nominalCurrency));
            dividendsIndex++;
        }

        final BigDecimal sellingAbsoluteReturnNominal = DecimalUtils.multiply(lastPrice, shareCount);
        final BigDecimal sellingAbsoluteReturnRub =
                extMarketDataService.convertCurrency(nominalCurrency, Currencies.RUB, sellingAbsoluteReturnNominal, to);
        absoluteReturn = absoluteReturn.add(sellingAbsoluteReturnRub);

        return getRelativeAnnualReturn(absoluteReturn, expenses, from, to);
    }

    private BigDecimal getDividendRub(final Dividend dividend, final int shareCount, final String nominalCurrency) {
        final BigDecimal dividendNominal = DecimalUtils.multiply(dividend.dividendNet(), shareCount);
        final OffsetDateTime paymentDate = dividend.paymentDate();
        return extMarketDataService.convertCurrency(nominalCurrency, Currencies.RUB, dividendNominal, paymentDate);
    }

    /**
     * @return (absoluteReturn / expenses) ^ (1 / (to - from)) - 1
     */
    private static double getRelativeAnnualReturn(
            final BigDecimal absoluteReturn,
            final BigDecimal expenses,
            final OffsetDateTime from,
            final OffsetDateTime to
    ) {
        final double relativeReturn = DecimalUtils.divide(absoluteReturn, expenses).doubleValue();
        final double yearsCount = Duration.between(from, to).toNanos() / (double) DateUtils.NANOSECONDS_PER_YEAR;
        return Math.pow(relativeReturn, 1 / yearsCount) - 1;
    }

}