package ru.obukhov.trader.trading.backtest.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.ExecutionResult;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.ExecutionUtils;
import ru.obukhov.trader.common.util.FinUtils;
import ru.obukhov.trader.common.util.MathUtils;
import ru.obukhov.trader.config.properties.BackTestProperties;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.transform.OperationMapper;
import ru.obukhov.trader.trading.backtest.interfaces.BackTester;
import ru.obukhov.trader.trading.bots.impl.FakeBot;
import ru.obukhov.trader.trading.bots.impl.FakeBotFactory;
import ru.obukhov.trader.trading.model.BackTestOperation;
import ru.obukhov.trader.trading.model.BackTestPosition;
import ru.obukhov.trader.trading.model.BackTestResult;
import ru.obukhov.trader.trading.model.Balances;
import ru.obukhov.trader.trading.model.Profits;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.Operation;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
    private final FakeBotFactory fakeBotFactory;
    private final ExecutorService executor;

    public BackTesterImpl(final ExcelService excelService, final FakeBotFactory fakeBotFactory, final BackTestProperties backTestProperties) {
        this.excelService = excelService;
        this.fakeBotFactory = fakeBotFactory;
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
        Assert.isTrue(interval.toDays() >= 1, "interval can't be shorter than 1 day");

        final Interval finiteInterval = interval.limitByNowIfNull(now);

        return botConfigs.stream()
                .map(botConfig -> startBackTest(botConfig, balanceConfig, finiteInterval))
                .toList().stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparing(result -> ((BackTestResult) result).balances().finalTotalSavings()).reversed())
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

    private BackTestResult test(final BotConfig botConfig, final BalanceConfig balanceConfig, final Interval interval) throws IOException {
        final FakeBot fakeBot = fakeBotFactory.createBot(botConfig, balanceConfig, interval.getFrom());

        final List<Candle> historicalCandles = new ArrayList<>();
        OffsetDateTime previousStartTime = null;

        do {
            final List<Candle> currentCandles = fakeBot.processBotConfig(botConfig, previousStartTime);
            if (currentCandles.isEmpty()) {
                previousStartTime = null;
            } else {
                previousStartTime = currentCandles.get(0).getTime();
                addLastCandle(historicalCandles, currentCandles);
            }

            moveToNextMinuteAndApplyBalanceIncrement(botConfig.getBrokerAccountId(), botConfig.getTicker(), balanceConfig, fakeBot, interval.getTo());
        } while (fakeBot.getCurrentDateTime().isBefore(interval.getTo()));

        return createSucceedBackTestResult(botConfig, interval, historicalCandles, fakeBot);
    }

    private void addLastCandle(final List<Candle> historicalCandles, final List<Candle> currentCandles) {
        final Candle candle = currentCandles.get(currentCandles.size() - 1);
        if (candle != null) {
            historicalCandles.add(candle);
        }
    }

    private void moveToNextMinuteAndApplyBalanceIncrement(
            @Nullable final String brokerAccountId,
            final String ticker,
            final BalanceConfig balanceConfig,
            final FakeBot fakeBot,
            final OffsetDateTime to
    ) throws IOException {
        if (balanceConfig.getBalanceIncrement() == null) {
            fakeBot.nextMinute();
        } else {
            final OffsetDateTime previousDate = fakeBot.getCurrentDateTime();
            final OffsetDateTime nextDate = DateUtils.getEarliestDateTime(fakeBot.nextMinute(), to);

            final List<OffsetDateTime> investmentsTimes = DateUtils.getCronHitsBetweenDates(balanceConfig.getBalanceIncrementCron(), previousDate, nextDate);
            final String currency = getCurrency(fakeBot, ticker);
            for (OffsetDateTime investmentTime : investmentsTimes) {
                fakeBot.addInvestment(brokerAccountId, investmentTime, currency, balanceConfig.getBalanceIncrement());
            }
        }
    }

    private BackTestResult createSucceedBackTestResult(
            final BotConfig botConfig,
            final Interval interval,
            final List<Candle> candles,
            final FakeBot fakeBot
    ) throws IOException {
        final String brokerAccountId = botConfig.getBrokerAccountId();
        final String ticker = botConfig.getTicker();

        final List<BackTestPosition> positions = getPositions(brokerAccountId, fakeBot);
        final Balances balances = getBalances(brokerAccountId, interval, fakeBot, positions, ticker);
        final Profits profits = getProfits(balances, interval);
        final List<Operation> operations = fakeBot.getOperations(brokerAccountId, interval, ticker);

        return new BackTestResult(
                botConfig,
                interval,
                balances,
                profits,
                positions,
                getOperations(operations, ticker),
                candles,
                null
        );
    }

    private BackTestResult createFailedBackTestResult(
            final BotConfig botConfig,
            final BigDecimal initialInvestment,
            final Interval interval,
            final String message
    ) {
        final Balances balances = new Balances(initialInvestment, initialInvestment, initialInvestment, BigDecimal.ZERO, BigDecimal.ZERO);
        return new BackTestResult(
                botConfig,
                interval,
                balances,
                Profits.ZEROS,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                message
        );
    }

    private String getCurrency(final FakeBot fakeBot, final String ticker) {
        return fakeBot.getShare(ticker).getCurrency();
    }

    private List<BackTestPosition> getPositions(final String brokerAccountId, final FakeBot fakeBot) throws IOException {
        List<PortfolioPosition> portfolioPositions = fakeBot.getPortfolioPositions(brokerAccountId);
        List<BackTestPosition> backTestPositions = new ArrayList<>(portfolioPositions.size());
        for (PortfolioPosition portfolioPosition : portfolioPositions) {
            backTestPositions.add(createBackTestPosition(portfolioPosition, fakeBot));
        }
        return backTestPositions;
    }

    private BackTestPosition createBackTestPosition(final PortfolioPosition portfolioPosition, final FakeBot fakeBot) throws IOException {
        final String ticker = portfolioPosition.ticker();
        return new BackTestPosition(ticker, fakeBot.getCurrentPrice(ticker), portfolioPosition.count());
    }

    private Balances getBalances(
            @Nullable final String brokerAccountId,
            final Interval interval,
            final FakeBot fakeBot,
            final List<BackTestPosition> positions,
            final String ticker
    ) {
        final String currency = getCurrency(fakeBot, ticker);
        final SortedMap<OffsetDateTime, BigDecimal> investments = fakeBot.getInvestments(brokerAccountId, currency);

        final BigDecimal initialInvestment = investments.get(investments.firstKey());
        final BigDecimal finalBalance = fakeBot.getCurrentBalance(brokerAccountId, currency);
        final BigDecimal finalTotalSavings = getTotalBalance(finalBalance, positions);

        final BigDecimal totalInvestment = investments.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        final BigDecimal weightedAverageInvestment = getWeightedAverage(investments, interval.getTo());

        return new Balances(initialInvestment, totalInvestment, weightedAverageInvestment, finalBalance, finalTotalSavings);
    }

    private BigDecimal getTotalBalance(final BigDecimal currentBalance, final List<BackTestPosition> positions) {
        return positions.stream()
                .map(position -> DecimalUtils.multiply(position.price(), position.quantity()))
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
        final BigDecimal absolute = balances.finalTotalSavings().subtract(balances.totalInvestment());
        final double relative = FinUtils.getRelativeProfit(balances.weightedAverageInvestment(), absolute);
        final double relativeAnnual = FinUtils.getAverageAnnualReturn(interval.toDays(), relative);
        return new Profits(absolute, relative, relativeAnnual);
    }

    private List<BackTestOperation> getOperations(final List<Operation> operations, final String ticker) {
        return operations.stream()
                .map(operation -> operationMapper.map(ticker, operation))
                .toList();
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