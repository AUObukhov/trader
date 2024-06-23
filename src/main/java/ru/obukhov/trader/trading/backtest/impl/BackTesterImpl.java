package ru.obukhov.trader.trading.backtest.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.quartz.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.ExecutionResult;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.ExecutionUtils;
import ru.obukhov.trader.common.util.FinUtils;
import ru.obukhov.trader.common.util.FirstCandleUtils;
import ru.obukhov.trader.common.util.MathUtils;
import ru.obukhov.trader.config.properties.BackTestProperties;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Instrument;
import ru.obukhov.trader.market.model.PositionUtils;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.trading.backtest.interfaces.BackTester;
import ru.obukhov.trader.trading.bots.FakeBot;
import ru.obukhov.trader.trading.bots.FakeBotFactory;
import ru.obukhov.trader.trading.model.BackTestResult;
import ru.obukhov.trader.trading.model.Balances;
import ru.obukhov.trader.trading.model.Profits;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Back tests trading by bots
 */
@Slf4j
@Service
public class BackTesterImpl implements BackTester {

    private final ExcelService excelService;
    private final ExtInstrumentsService extInstrumentsService;
    private final ExtMarketDataService extMarketDataService;
    private final FakeBotFactory fakeBotFactory;
    private final ExecutorService executor;

    public BackTesterImpl(
            final ExcelService excelService,
            final ExtInstrumentsService extInstrumentsService,
            final ExtMarketDataService extMarketDataService,
            final FakeBotFactory fakeBotFactory,
            final BackTestProperties backTestProperties
    ) {
        this.excelService = excelService;
        this.extInstrumentsService = extInstrumentsService;
        this.extMarketDataService = extMarketDataService;
        this.fakeBotFactory = fakeBotFactory;
        this.executor = Executors.newFixedThreadPool(backTestProperties.getThreadCount());
    }

    /**
     * @param botConfigs    bot configurations of each back test
     * @param balanceConfig all back tests balance configuration
     * @param interval      all back tests interval
     * @param saveToFiles   flag to save back tests results to file
     * @return list of back tests results
     */
    @Override
    public List<BackTestResult> test(
            final List<BotConfig> botConfigs,
            final BalanceConfig balanceConfig,
            final Interval interval,
            final boolean saveToFiles
    ) {
        log.info("Back test started");

        ExecutionResult<List<BackTestResult>> executionResult = ExecutionUtils.get(() -> test(botConfigs, balanceConfig, interval));

        final String backTestDurationString = DurationFormatUtils.formatDurationHMS(executionResult.duration().toMillis());
        log.info("Back test ended within {}", backTestDurationString);

        if (saveToFiles) {
            saveBackTestResultsSafe(executionResult.result());
        }

        return executionResult.result();
    }

    private List<BackTestResult> test(final List<BotConfig> botConfigs, final BalanceConfig balanceConfig, final Interval interval) {
        final OffsetDateTime now = DateUtils.now();
        DateUtils.assertDateTimeNotFuture(interval.getFrom(), now, "from");
        DateUtils.assertDateTimeNotFuture(interval.getTo(), now, "to");
        Assert.isTrue(interval.toDays() >= 1, "interval can't be shorter than 1 day");

        final Interval finiteInterval = interval.limitByNowIfNull(now);

        return botConfigs.stream()
                .map(botConfig -> startBackTest(botConfig, balanceConfig, finiteInterval))
                .toList().stream()
                .map(CompletableFuture::join)
                .toList();
    }

    private CompletableFuture<BackTestResult> startBackTest(
            final BotConfig botConfig,
            final BalanceConfig balanceConfig,
            final Interval interval
    ) {
        return CompletableFuture.supplyAsync(() -> backTestSafe(botConfig, balanceConfig, interval), executor);
    }

    private BackTestResult backTestSafe(final BotConfig botConfig, final BalanceConfig balanceConfig, final Interval interval) {
        log.info("Starting back test for '{}'", botConfig);

        ExecutionResult<BackTestResult> executionResult = ExecutionUtils.getSafe(() -> test(botConfig, balanceConfig, interval));

        final String backTestDurationString = DurationFormatUtils.formatDurationHMS(executionResult.duration().toMillis());

        if (executionResult.exception() == null) {
            log.info("Back test for '{}' succeed within {}", botConfig, backTestDurationString);
            return executionResult.result();
        } else {
            final String message = String.format(
                    "Back test for '%s' failed within %s with error: %s",
                    botConfig, backTestDurationString, executionResult.exception().getMessage()
            );
            log.error(message, executionResult.exception());
            return createFailedBackTestResult(botConfig, balanceConfig.getInitialBalances(), interval, message);
        }
    }

    private BackTestResult test(final BotConfig botConfig, final BalanceConfig balanceConfig, final Interval interval) {
        final Interval effectiveInterval = getEffectiveInterval(botConfig, interval);
        final FakeBot fakeBot = fakeBotFactory.createBot(botConfig, balanceConfig, effectiveInterval.getFrom());

        final CandleInterval candleInterval = botConfig.candleInterval();
        final Map<String, List<Candle>> candles = botConfig.figies().stream()
                .collect(Collectors.toMap(Function.identity(), figi -> extMarketDataService.getCandles(figi, effectiveInterval, candleInterval)));

        do {
            fakeBot.processBotConfig(botConfig, effectiveInterval);

            moveToNextMinuteAndApplyBalanceIncrement(botConfig.accountId(), botConfig.figies(), balanceConfig, fakeBot, effectiveInterval.getTo());
        } while (fakeBot.getCurrentDateTime() != null && fakeBot.getCurrentDateTime().isBefore(effectiveInterval.getTo()));

        return createSucceedBackTestResult(botConfig, effectiveInterval, candles, fakeBot);
    }

    private Interval getEffectiveInterval(final BotConfig botConfig, final Interval interval) {
        return botConfig.figies().stream()
                .map(figi -> getEffectiveInterval(figi, botConfig.candleInterval(), interval))
                .reduce(Interval::unite)
                .orElseThrow();
    }

    private Interval getEffectiveInterval(final String figi, final CandleInterval candleInterval, final Interval interval) {
        final Instrument instrument = extInstrumentsService.getInstrument(figi);
        final OffsetDateTime firstCandleDate = getFirstCandleDate(candleInterval, instrument);
        if (interval.getFrom().isBefore(firstCandleDate)) {
            final Interval effectiveInterval = Interval.of(firstCandleDate, interval.getTo());

            final String format = "First candle of instrument {} starts on {}. Effective interval for back test is {}";
            log.info(format, figi, firstCandleDate, effectiveInterval);

            return effectiveInterval;
        } else {
            return interval;
        }
    }

    private static OffsetDateTime getFirstCandleDate(CandleInterval candleInterval, Instrument instrument) {
        final OffsetDateTime first1MinCandleDate = instrument.first1MinCandleDate();
        final OffsetDateTime first1DayCandleDate = instrument.first1DayCandleDate();
        return FirstCandleUtils.getFirstCandleDate(first1MinCandleDate, first1DayCandleDate, candleInterval);
    }

    private void moveToNextMinuteAndApplyBalanceIncrement(
            final String accountId,
            final List<String> figies,
            final BalanceConfig balanceConfig,
            final FakeBot fakeBot,
            final OffsetDateTime to
    ) {
        if (MapUtils.isEmpty(balanceConfig.getBalanceIncrements())) {
            final Interval interval = Interval.of(fakeBot.getCurrentDateTime(), to);
            final List<TradingDay> tradingSchedule = extInstrumentsService.getTradingScheduleByFigies(figies, interval);
            fakeBot.nextScheduleMinute(tradingSchedule);
            return;
        }

        final OffsetDateTime previousDate = fakeBot.getCurrentDateTime();
        if (!previousDate.isBefore(to)) {
            return;
        }

        final Interval interval = Interval.of(previousDate, to);
        final List<TradingDay> tradingSchedule = extInstrumentsService.getTradingScheduleByFigies(figies, interval);
        final OffsetDateTime nextScheduleMinute = fakeBot.nextScheduleMinute(tradingSchedule);
        final OffsetDateTime nextDate = DateUtils.getEarliestDateTime(nextScheduleMinute, to);

        final CronExpression balanceIncrementCron = balanceConfig.getBalanceIncrementCron();
        final List<OffsetDateTime> investmentsTimes = DateUtils.getCronHitsBetweenDates(balanceIncrementCron, previousDate, nextDate);
        for (final OffsetDateTime investmentTime : investmentsTimes) {
            fakeBot.addInvestments(accountId, investmentTime, balanceConfig.getBalanceIncrements());
        }
    }

    private BackTestResult createSucceedBackTestResult(
            final BotConfig botConfig,
            final Interval interval,
            final Map<String, List<Candle>> candles,
            final FakeBot fakeBot
    ) {
        final String accountId = botConfig.accountId();
        final List<String> figies = botConfig.figies();

        final List<Position> positions = getPositions(fakeBot, accountId, interval.getTo());
        final Map<String, Balances> balances = getBalances(accountId, interval, fakeBot, positions, figies);
        final Map<String, Profits> profits = getProfits(balances, interval);
        final Map<String, List<Operation>> operations = fakeBot.getOperations(accountId, interval, figies);

        return new BackTestResult(
                botConfig,
                interval,
                balances,
                profits,
                positions,
                operations,
                candles,
                null
        );
    }

    private BackTestResult createFailedBackTestResult(
            final BotConfig botConfig,
            final Map<String, BigDecimal> initialBalances,
            final Interval interval,
            final String message
    ) {
        final Map<String, Balances> balances = new HashMap<>();
        for (final Map.Entry<String, BigDecimal> entry : initialBalances.entrySet()) {
            final Balances currentBalances = new Balances(
                    entry.getValue(),
                    entry.getValue(),
                    entry.getValue(),
                    DecimalUtils.ZERO,
                    DecimalUtils.ZERO
            );
            balances.put(entry.getKey(), currentBalances);
        }
        final Map<String, List<Operation>> operations = botConfig.figies().stream()
                .collect(Collectors.toMap(Function.identity(), figi -> Collections.emptyList()));
        final Map<String, List<Candle>> candles = botConfig.figies().stream()
                .collect(Collectors.toMap(Function.identity(), figi -> Collections.emptyList()));

        return new BackTestResult(
                botConfig,
                interval,
                balances,
                Collections.emptyMap(),
                Collections.emptyList(),
                operations,
                candles,
                message
        );
    }

    private List<Position> getPositions(final FakeBot fakeBot, final String accountId, final OffsetDateTime dateTime) {
        return fakeBot.getPortfolioPositions(accountId).stream()
                .map(position -> cloneWithActualCurrentPrice(position, fakeBot, dateTime))
                .toList();
    }

    private Position cloneWithActualCurrentPrice(
            final Position portfolioPosition,
            final FakeBot fakeBot,
            final OffsetDateTime dateTime
    ) {
        final String figi = portfolioPosition.getFigi();
        final BigDecimal currentPrice = fakeBot.getCurrentPrice(figi, dateTime);
        return PositionUtils.cloneWithNewCurrentPrice(portfolioPosition, currentPrice);
    }

    private Map<String, Balances> getBalances(
            final String accountId,
            final Interval interval,
            final FakeBot fakeBot,
            final List<Position> positions,
            final List<String> figies
    ) {
        return figies.stream()
                .map(figi -> extInstrumentsService.getShare(figi).currency())
                .distinct()
                .collect(Collectors.toMap(Function.identity(), currency -> getBalances(accountId, interval, fakeBot, positions, currency)));
    }

    private Balances getBalances(
            final String accountId,
            final Interval interval,
            final FakeBot fakeBot,
            final List<Position> positions,
            final String currency
    ) {
        final SortedMap<OffsetDateTime, BigDecimal> investments = fakeBot.getInvestments(accountId, currency);

        final BigDecimal initialInvestment = investments.get(investments.firstKey());
        final BigDecimal finalBalance = fakeBot.getCurrentBalance(accountId, currency);
        final BigDecimal finalTotalSavings = getTotalBalance(finalBalance, positions);

        final BigDecimal totalInvestment = investments.values().stream().reduce(DecimalUtils.ZERO, BigDecimal::add);
        final BigDecimal weightedAverageInvestment = getWeightedAverage(investments, interval.getTo());

        return new Balances(initialInvestment, totalInvestment, weightedAverageInvestment, finalBalance, finalTotalSavings);
    }

    private BigDecimal getTotalBalance(final BigDecimal currentBalance, final List<Position> positions) {
        return positions.stream()
                .map(position -> position.getCurrentPrice().getValue().multiply(position.getQuantity()))
                .reduce(currentBalance, BigDecimal::add);
    }

    private BigDecimal getWeightedAverage(final SortedMap<OffsetDateTime, BigDecimal> investments, final OffsetDateTime endDateTime) {
        final SortedMap<OffsetDateTime, BigDecimal> totalInvestments = getTotalInvestments(investments);
        return MathUtils.getWeightedAverage(totalInvestments, endDateTime);
    }

    private SortedMap<OffsetDateTime, BigDecimal> getTotalInvestments(final SortedMap<OffsetDateTime, BigDecimal> investments) {
        final SortedMap<OffsetDateTime, BigDecimal> balances = new TreeMap<>();
        BigDecimal currentBalance = DecimalUtils.ZERO;
        for (final Map.Entry<OffsetDateTime, BigDecimal> entry : investments.entrySet()) {
            currentBalance = currentBalance.add(entry.getValue());
            balances.put(entry.getKey(), currentBalance);
        }
        return balances;
    }

    private Map<String, Profits> getProfits(final Map<String, Balances> balances, final Interval interval) {
        return balances.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> getProfits(entry.getValue(), interval)));
    }

    private Profits getProfits(final Balances balances, final Interval interval) {
        final BigDecimal absolute = balances.finalTotalSavings().subtract(balances.totalInvestment());
        final double relative = FinUtils.getRelativeProfit(balances.weightedAverageInvestment(), absolute);
        final double relativeAnnual = FinUtils.getAverageAnnualReturn(interval.toDays(), relative);
        return new Profits(absolute, relative, relativeAnnual);
    }

    private void saveBackTestResultsSafe(final List<BackTestResult> backTestResults) {
        try {
            log.debug("Saving back test result to file");
            excelService.saveBackTestResults(backTestResults);
        } catch (Exception ex) {
            log.error("Failed to save back test result to file", ex);
        }
    }

}