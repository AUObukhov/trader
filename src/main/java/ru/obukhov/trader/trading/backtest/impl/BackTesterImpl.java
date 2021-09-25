package ru.obukhov.trader.trading.backtest.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.common.model.ExecutionResult;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.CollectionsUtils;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.ExecutionUtils;
import ru.obukhov.trader.common.util.MathUtils;
import ru.obukhov.trader.config.properties.BackTestProperties;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.transform.OperationMapper;
import ru.obukhov.trader.trading.backtest.interfaces.BackTester;
import ru.obukhov.trader.trading.bots.interfaces.BotFactory;
import ru.obukhov.trader.trading.bots.interfaces.FakeBot;
import ru.obukhov.trader.trading.model.BackTestOperation;
import ru.obukhov.trader.trading.model.BackTestPosition;
import ru.obukhov.trader.trading.model.BackTestResult;
import ru.obukhov.trader.trading.model.Balances;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.Profits;
import ru.obukhov.trader.trading.strategy.impl.AbstractTradingStrategy;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.Operation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Back tests trading by bots
 */
@Slf4j
@Service
public class BackTesterImpl implements BackTester {

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);

    private final ExcelService excelService;
    private final BotFactory fakeBotFactory;
    private final ExecutorService executor;
    private final TradingStrategyFactory strategyFactory;

    public BackTesterImpl(
            final ExcelService excelService,
            final BotFactory fakeBotFactory,
            final TradingStrategyFactory strategyFactory,
            final BackTestProperties backTestProperties
    ) {
        this.excelService = excelService;
        this.fakeBotFactory = fakeBotFactory;
        this.strategyFactory = strategyFactory;
        this.executor = Executors.newFixedThreadPool(backTestProperties.getThreadCount());
    }

    /**
     * @param botConfigs    bot configurations of each back test
     * @param balanceConfig all back tests balance configuration
     * @param interval      all back tests interval
     * @param saveToFiles   flag to save back tests results to file
     * @return map of back tests results by tickers
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

        final String backTestDurationString = DurationFormatUtils.formatDurationHMS(executionResult.getDuration().toMillis());
        log.info("Back test ended within {}", backTestDurationString);

        if (saveToFiles) {
            saveBackTestResultsSafe(executionResult.getResult());
        }

        return executionResult.getResult();
    }

    private List<BackTestResult> test(final List<BotConfig> botConfigs, final BalanceConfig balanceConfig, final Interval interval) {
        final OffsetDateTime now = OffsetDateTime.now();
        DateUtils.assertDateTimeNotFuture(interval.getFrom(), now, "from");
        DateUtils.assertDateTimeNotFuture(interval.getTo(), now, "to");

        final Interval finiteInterval = interval.limitByNowIfNull(now);

        final List<CompletableFuture<BackTestResult>> backTestFutures = botConfigs.stream()
                .map(botConfig -> startBackTest(botConfig, balanceConfig, finiteInterval))
                .toList();
        return backTestFutures.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparing(result -> ((BackTestResult) result).getBalances().getFinalTotalSavings()).reversed())
                .toList();
    }

    private FakeBot createFakeBot(final BotConfig botConfig) {
        final AbstractTradingStrategy strategy = strategyFactory.createStrategy(botConfig);
        return (FakeBot) fakeBotFactory.createBot(strategy, botConfig.getCandleResolution());
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

        final String backTestDurationString = DurationFormatUtils.formatDurationHMS(executionResult.getDuration().toMillis());

        if (executionResult.getException() == null) {
            log.info("Back test for '{}' succeed within {}", botConfig, backTestDurationString);
            return executionResult.getResult();
        } else {
            final String message = String.format(
                    "Back test for '%s' failed within %s with error: %s",
                    botConfig, backTestDurationString, executionResult.getException().getMessage()
            );
            log.error(message, executionResult.getException());
            return createFailedBackTestResult(botConfig, balanceConfig.getInitialBalance(), interval, message);
        }
    }

    private BackTestResult test(final BotConfig botConfig, final BalanceConfig balanceConfig, final Interval interval) {
        final FakeBot bot = createFakeBot(botConfig);

        final FakeTinkoffService fakeTinkoffService = bot.getFakeTinkoffService();

        final String brokerAccountId = botConfig.getBrokerAccountId();
        final String ticker = botConfig.getTicker();
        final MarketInstrument marketInstrument = fakeTinkoffService.searchMarketInstrument(ticker);
        if (marketInstrument == null) {
            throw new IllegalArgumentException("Not found instrument for ticker '" + ticker + "'");
        }

        fakeTinkoffService.init(brokerAccountId, interval.getFrom(), marketInstrument.getCurrency(), balanceConfig.getInitialBalance());
        final List<Candle> historicalCandles = new ArrayList<>();
        OffsetDateTime previousStartTime = null;

        do {
            final DecisionData decisionData = bot.processTicker(brokerAccountId, ticker, previousStartTime, fakeTinkoffService.getCurrentDateTime());
            final List<Candle> currentCandles = decisionData.getCurrentCandles();
            if (CollectionUtils.isEmpty(currentCandles)) {
                previousStartTime = null;
            } else {
                previousStartTime = currentCandles.get(0).getTime();
                addLastCandle(historicalCandles, currentCandles);
            }

            moveToNextMinute(ticker, balanceConfig, fakeTinkoffService);
        } while (fakeTinkoffService.getCurrentDateTime().isBefore(interval.getTo()));

        return createSucceedBackTestResult(botConfig, interval, historicalCandles, bot.getFakeTinkoffService());
    }

    private void addLastCandle(final List<Candle> historicalCandles, final List<Candle> currentCandles) {
        final Candle candle = currentCandles.get(currentCandles.size() - 1);
        if (candle != null) {
            historicalCandles.add(candle);
        }
    }

    private void moveToNextMinute(final String ticker, final BalanceConfig balanceConfig, final FakeTinkoffService fakeTinkoffService) {
        if (balanceConfig.getBalanceIncrement() == null) {
            fakeTinkoffService.nextMinute();
        } else {
            final OffsetDateTime previousDate = fakeTinkoffService.getCurrentDateTime();
            final OffsetDateTime nextDate = fakeTinkoffService.nextMinute();

            final int incrementsCount = DateUtils.getCronHitsBetweenDates(balanceConfig.getBalanceIncrementCron(), previousDate, nextDate);
            if (incrementsCount > 0) {
                final BigDecimal totalBalanceIncrement = DecimalUtils.multiply(balanceConfig.getBalanceIncrement(), incrementsCount);
                final Currency currency = getCurrency(fakeTinkoffService, ticker);
                final BigDecimal currentBalance = fakeTinkoffService.getCurrentBalance(currency);
                log.debug("Incrementing balance {} by {}", currentBalance, totalBalanceIncrement);
                fakeTinkoffService.incrementBalance(currency, totalBalanceIncrement);
            }
        }
    }

    private BackTestResult createSucceedBackTestResult(
            final BotConfig botConfig,
            final Interval interval,
            final List<Candle> candles,
            final FakeTinkoffService fakeTinkoffService
    ) {
        final String brokerAccountId = botConfig.getBrokerAccountId();
        final String ticker = botConfig.getTicker();

        final List<BackTestPosition> positions = getPositions(fakeTinkoffService.getPortfolioPositions(brokerAccountId), candles);
        final Balances balances = getBalances(interval, fakeTinkoffService, positions, ticker);
        final Profits profits = getProfits(balances, interval);
        final List<Operation> operations = fakeTinkoffService.getOperations(brokerAccountId, interval, ticker);

        return BackTestResult.builder()
                .botConfig(botConfig)
                .interval(interval)
                .balances(balances)
                .profits(profits)
                .positions(positions)
                .operations(getOperations(operations, ticker))
                .candles(candles)
                .build();
    }

    private BackTestResult createFailedBackTestResult(
            final BotConfig botConfig,
            final BigDecimal initialInvestment,
            final Interval interval,
            final String message
    ) {
        final Balances balances = new Balances(initialInvestment, initialInvestment, initialInvestment, BigDecimal.ZERO, BigDecimal.ZERO);
        return BackTestResult.builder()
                .botConfig(botConfig)
                .interval(interval)
                .balances(balances)
                .profits(Profits.ZEROS)
                .positions(Collections.emptyList())
                .operations(Collections.emptyList())
                .candles(Collections.emptyList())
                .error(message)
                .build();
    }

    private Currency getCurrency(final FakeTinkoffService fakeTinkoffService, final String ticker) {
        return fakeTinkoffService.searchMarketInstrument(ticker).getCurrency();
    }

    private List<BackTestPosition> getPositions(final Collection<PortfolioPosition> portfolioPositions, final List<Candle> candles) {
        final Candle lastCandle = CollectionsUtils.getLast(candles);
        final BigDecimal currentPrice = lastCandle == null ? null : lastCandle.getClosePrice();

        return portfolioPositions.stream()
                .map(portfolioPosition -> createBackTestPosition(portfolioPosition, currentPrice))
                .toList();
    }

    private BackTestPosition createBackTestPosition(final PortfolioPosition portfolioPosition, final BigDecimal currentPrice) {
        return new BackTestPosition(portfolioPosition.getTicker(), currentPrice, portfolioPosition.getCount());
    }

    private Balances getBalances(
            final Interval interval,
            final FakeTinkoffService fakeTinkoffService,
            final List<BackTestPosition> positions,
            final String ticker
    ) {
        final Currency currency = getCurrency(fakeTinkoffService, ticker);
        final SortedMap<OffsetDateTime, BigDecimal> investments = fakeTinkoffService.getInvestments(currency);

        final BigDecimal initialInvestment = investments.get(investments.firstKey());
        final BigDecimal finalBalance = fakeTinkoffService.getCurrentBalance(currency);
        final BigDecimal finalTotalSavings = getTotalBalance(finalBalance, positions);

        final BigDecimal totalInvestment = investments.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        final BigDecimal weightedAverageInvestment = getWeightedAverage(investments, interval.getTo());

        return new Balances(initialInvestment, totalInvestment, weightedAverageInvestment, finalBalance, finalTotalSavings);
    }

    private BigDecimal getTotalBalance(final BigDecimal currentBalance, final List<BackTestPosition> positions) {
        return positions.stream()
                .map(position -> DecimalUtils.multiply(position.getPrice(), position.getQuantity()))
                .reduce(currentBalance, BigDecimal::add);
    }

    private BigDecimal getWeightedAverage(final SortedMap<OffsetDateTime, BigDecimal> investments, final OffsetDateTime endDateTime) {
        final SortedMap<OffsetDateTime, BigDecimal> totalInvestments = getTotalInvestments(investments);
        return MathUtils.getWeightedAverage(totalInvestments, endDateTime);
    }

    private SortedMap<OffsetDateTime, BigDecimal> getTotalInvestments(final SortedMap<OffsetDateTime, BigDecimal> investments) {
        final SortedMap<OffsetDateTime, BigDecimal> balances = new TreeMap<>();
        BigDecimal currentBalance = BigDecimal.ZERO;
        for (final Map.Entry<OffsetDateTime, BigDecimal> entry : investments.entrySet()) {
            currentBalance = currentBalance.add(entry.getValue());
            balances.put(entry.getKey(), currentBalance);
        }
        return balances;
    }

    private Profits getProfits(final Balances balances, final Interval interval) {
        final BigDecimal absoluteProfit = balances.getFinalTotalSavings().subtract(balances.getTotalInvestment());
        final double relativeProfit = getRelativeProfit(balances.getWeightedAverageInvestment(), absoluteProfit);
        final double averageAnnualProfitability = getRelativeYearProfit(interval, relativeProfit);
        return new Profits(absoluteProfit, relativeProfit, averageAnnualProfitability);
    }

    private double getRelativeProfit(BigDecimal weightedAverageInvestment, BigDecimal absoluteProfit) {
        return weightedAverageInvestment.signum() == 0
                ? 0.0
                : DecimalUtils.divide(absoluteProfit, weightedAverageInvestment).doubleValue();
    }

    private double getRelativeYearProfit(final Interval interval, final double relativeProfit) {
        final BigDecimal partOfYear = BigDecimal.valueOf(interval.toDays() / DateUtils.DAYS_IN_YEAR);
        return BigDecimal.valueOf(relativeProfit).divide(partOfYear, RoundingMode.HALF_UP).doubleValue();
    }

    private List<BackTestOperation> getOperations(final List<Operation> operations, final String ticker) {
        final List<BackTestOperation> backTestOperations = operations.stream()
                .map(operationMapper::map)
                .toList();
        backTestOperations.forEach(operation -> operation.setTicker(ticker));
        return backTestOperations;
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