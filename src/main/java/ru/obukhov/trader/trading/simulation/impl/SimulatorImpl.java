package ru.obukhov.trader.trading.simulation.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.mapstruct.factory.Mappers;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.CollectionsUtils;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.MathUtils;
import ru.obukhov.trader.config.TradingConfig;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.transform.OperationMapper;
import ru.obukhov.trader.trading.bots.interfaces.BotFactory;
import ru.obukhov.trader.trading.bots.interfaces.FakeBot;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.simulation.interfaces.Simulator;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;
import ru.obukhov.trader.web.model.pojo.SimulatedPosition;
import ru.obukhov.trader.web.model.pojo.SimulationResult;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.Operation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
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
import java.util.stream.Collectors;

/**
 * Simulates trading by bots
 */
@Slf4j
@Service
public class SimulatorImpl implements Simulator {

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);

    private final ExcelService excelService;
    private final BotFactory fakeBotFactory;
    private final ExecutorService executor;
    private final TradingStrategyFactory strategyFactory;

    public SimulatorImpl(
            final ExcelService excelService,
            final BotFactory fakeBotFactory,
            final TradingStrategyFactory strategyFactory,
            @Value("${simulation.thread-count:10}") final Integer simulationThreadCount
    ) {
        Assert.isTrue(simulationThreadCount > 1, "simulationThreadCount must be greater than 1");

        this.excelService = excelService;
        this.fakeBotFactory = fakeBotFactory;
        this.strategyFactory = strategyFactory;
        this.executor = Executors.newFixedThreadPool(simulationThreadCount);
    }

    /**
     * @param ticker               ticker for all simulations
     * @param initialBalance       initial balance sum of each simulation
     * @param balanceIncrement     regular balance increment sum
     * @param balanceIncrementCron cron expression, representing schedule of balance increment
     * @param interval             all simulations interval
     * @param saveToFiles          flag to save simulations results to file
     * @return map of simulations results by tickers
     */
    @Override
    public List<SimulationResult> simulate(
            final String ticker,
            final BigDecimal initialBalance,
            final BigDecimal balanceIncrement,
            final CronExpression balanceIncrementCron,
            final List<TradingConfig> tradingConfigs,
            final Interval interval,
            final boolean saveToFiles
    ) {
        log.info("Simulation for ticker = '{}' started", ticker);

        final OffsetDateTime startTime = OffsetDateTime.now();
        DateUtils.assertDateTimeNotFuture(interval.getFrom(), startTime, "from");
        DateUtils.assertDateTimeNotFuture(interval.getTo(), startTime, "to");

        final Interval finiteInterval = interval.limitByNowIfNull();

        final List<CompletableFuture<SimulationResult>> simulationFutures = tradingConfigs.stream()
                .map(tradingConfig -> startSimulation(
                        tradingConfig,
                        ticker,
                        initialBalance,
                        balanceIncrement,
                        balanceIncrementCron,
                        finiteInterval
                ))
                .collect(Collectors.toList());
        final List<SimulationResult> simulationResults = simulationFutures.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparing(SimulationResult::getFinalTotalBalance).reversed())
                .collect(Collectors.toList());

        final Duration simulationDuration = Duration.between(startTime, OffsetDateTime.now());
        final String simulationDurationString = DurationFormatUtils.formatDurationHMS(simulationDuration.toMillis());
        log.info("Simulation for ticker = '{}' ended within {}", ticker, simulationDurationString);

        if (saveToFiles) {
            saveSimulationResultsSafe(ticker, simulationResults);
        }

        return simulationResults;
    }

    private FakeBot createFakeBot(final TradingConfig tradingConfig) {
        final TradingStrategy strategy = strategyFactory.createStrategy(
                tradingConfig.getStrategyType(),
                tradingConfig.getStrategyParams()
        );
        return (FakeBot) fakeBotFactory.createBot(strategy, tradingConfig.getCandleResolution());
    }

    private CompletableFuture<SimulationResult> startSimulation(
            final TradingConfig tradingConfig,
            final String ticker,
            final BigDecimal initialBalance,
            final BigDecimal balanceIncrement,
            final CronExpression balanceIncrementCron,
            final Interval interval
    ) {
        return CompletableFuture.supplyAsync(
                () -> simulateSafe(tradingConfig, ticker, initialBalance, balanceIncrement, balanceIncrementCron, interval),
                executor
        );
    }

    private SimulationResult simulateSafe(
            final TradingConfig tradingConfig,
            final String ticker,
            final BigDecimal initialBalance,
            final BigDecimal balanceIncrement,
            final CronExpression balanceIncrementCron,
            final Interval interval
    ) {
        try {
            log.info("Simulation for '{}' with ticker = '{}' started", tradingConfig, ticker);

            final SimulationResult result = simulate(
                    tradingConfig,
                    ticker,
                    initialBalance,
                    balanceIncrement,
                    balanceIncrementCron,
                    interval
            );
            log.info("Simulation for '{}' with ticker = '{}' ended", tradingConfig, ticker);
            return result;
        } catch (Exception ex) {
            final String message = String.format(
                    "Simulation for '%s' with ticker '%s' failed with error: %s",
                    tradingConfig, ticker, ex.getMessage()
            );
            log.error(message, ex);
            return SimulationResult.builder()
                    .tradingConfig(tradingConfig)
                    .interval(interval)
                    .initialBalance(initialBalance)
                    .totalInvestment(initialBalance)
                    .weightedAverageInvestment(initialBalance)
                    .finalBalance(BigDecimal.ZERO)
                    .finalTotalBalance(BigDecimal.ZERO)
                    .absoluteProfit(BigDecimal.ZERO)
                    .relativeProfit(0.0)
                    .relativeYearProfit(0.0)
                    .positions(Collections.emptyList())
                    .operations(Collections.emptyList())
                    .candles(Collections.emptyList())
                    .error(message)
                    .build();
        }
    }

    private SimulationResult simulate(
            final TradingConfig tradingConfig,
            final String ticker,
            final BigDecimal initialBalance,
            final BigDecimal balanceIncrement,
            final CronExpression balanceIncrementCron,
            final Interval interval
    ) {
        final FakeBot bot = createFakeBot(tradingConfig);

        final FakeTinkoffService fakeTinkoffService = bot.getFakeTinkoffService();

        final MarketInstrument marketInstrument = fakeTinkoffService.searchMarketInstrument(ticker);
        if (marketInstrument == null) {
            throw new IllegalArgumentException("Not found instrument for ticker '" + ticker + "'");
        }

        fakeTinkoffService.init(interval.getFrom(), marketInstrument.getCurrency(), initialBalance);
        final List<Candle> historicalCandles = new ArrayList<>();
        OffsetDateTime previousStartTime = null;

        do {
            final DecisionData decisionData = bot.processTicker(ticker, previousStartTime);
            final List<Candle> currentCandles = decisionData.getCurrentCandles();
            if (CollectionUtils.isEmpty(currentCandles)) {
                previousStartTime = null;
            } else {
                previousStartTime = currentCandles.get(0).getTime();
                addLastCandle(historicalCandles, currentCandles);
            }

            moveToNextMinute(ticker, balanceIncrement, balanceIncrementCron, fakeTinkoffService);
        } while (fakeTinkoffService.getCurrentDateTime().isBefore(interval.getTo()));

        return createResult(tradingConfig, interval, ticker, historicalCandles, bot.getFakeTinkoffService());
    }

    private void addLastCandle(final List<Candle> historicalCandles, final List<Candle> currentCandles) {
        final Candle candle = currentCandles.get(currentCandles.size() - 1);
        if (candle != null) {
            historicalCandles.add(candle);
        }
    }

    private void moveToNextMinute(
            final String ticker,
            final BigDecimal balanceIncrement,
            final CronExpression balanceIncrementCron,
            final FakeTinkoffService fakeTinkoffService
    ) {
        if (balanceIncrement == null) {
            fakeTinkoffService.nextMinute();
        } else {
            final OffsetDateTime previousDate = fakeTinkoffService.getCurrentDateTime();
            final OffsetDateTime nextDate = fakeTinkoffService.nextMinute();

            final int incrementsCount = DateUtils.getCronHitsBetweenDates(balanceIncrementCron, previousDate, nextDate);
            if (incrementsCount > 0) {
                final BigDecimal totalBalanceIncrement = DecimalUtils.multiply(balanceIncrement, incrementsCount);
                final Currency currency = getCurrency(fakeTinkoffService, ticker);
                final BigDecimal currentBalance = fakeTinkoffService.getCurrentBalance(currency);
                log.debug("Incrementing balance {} by {}", currentBalance, totalBalanceIncrement);
                fakeTinkoffService.incrementBalance(currency, totalBalanceIncrement);
            }
        }
    }

    private SimulationResult createResult(
            final TradingConfig tradingConfig,
            final Interval interval,
            final String ticker,
            final List<Candle> candles,
            final FakeTinkoffService fakeTinkoffService
    ) {
        final List<SimulatedPosition> positions = getPositions(fakeTinkoffService.getPortfolioPositions(), candles);
        final Currency currency = getCurrency(fakeTinkoffService, ticker);

        final SortedMap<OffsetDateTime, BigDecimal> investments = fakeTinkoffService.getInvestments(currency);

        final BigDecimal initialBalance = investments.get(investments.firstKey());
        final BigDecimal currentBalance = fakeTinkoffService.getCurrentBalance(currency);
        final BigDecimal totalBalance = getTotalBalance(currentBalance, positions);

        final BigDecimal totalInvestment = investments.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        final BigDecimal weightedAverageInvestment = getWeightedAverage(investments, interval.getTo());

        final BigDecimal absoluteProfit = totalBalance.subtract(totalInvestment);
        final double relativeProfit = DecimalUtils.divide(absoluteProfit, weightedAverageInvestment).doubleValue();
        final double relativeYearProfit = getRelativeYearProfit(interval, relativeProfit);
        final List<Operation> operations = fakeTinkoffService.getOperations(interval, ticker);

        return SimulationResult.builder()
                .tradingConfig(tradingConfig)
                .interval(interval)
                .initialBalance(initialBalance)
                .finalTotalBalance(totalBalance)
                .finalBalance(currentBalance)
                .totalInvestment(totalInvestment)
                .weightedAverageInvestment(weightedAverageInvestment)
                .absoluteProfit(absoluteProfit)
                .relativeProfit(relativeProfit)
                .relativeYearProfit(relativeYearProfit)
                .positions(positions)
                .operations(getOperations(operations, ticker))
                .candles(candles)
                .build();
    }

    private Currency getCurrency(final FakeTinkoffService fakeTinkoffService, final String ticker) {
        return fakeTinkoffService.searchMarketInstrument(ticker).getCurrency();
    }

    private List<SimulatedPosition> getPositions(
            final Collection<PortfolioPosition> portfolioPositions,
            final List<Candle> candles
    ) {
        final Candle lastCandle = CollectionsUtils.getLast(candles);
        final BigDecimal currentPrice = lastCandle == null ? null : lastCandle.getClosePrice();

        return portfolioPositions.stream()
                .map(portfolioPosition -> createSimulatedPosition(portfolioPosition, currentPrice))
                .collect(Collectors.toList());
    }

    private SimulatedPosition createSimulatedPosition(
            final PortfolioPosition portfolioPosition,
            final BigDecimal currentPrice
    ) {
        return new SimulatedPosition(portfolioPosition.getTicker(), currentPrice, portfolioPosition.getLotsCount());
    }

    private BigDecimal getTotalBalance(final BigDecimal balance, final List<SimulatedPosition> positions) {
        return positions.stream()
                .map(position -> DecimalUtils.multiply(position.getPrice(), position.getQuantity()))
                .reduce(balance, BigDecimal::add);
    }

    private BigDecimal getWeightedAverage(
            final SortedMap<OffsetDateTime, BigDecimal> investments,
            final OffsetDateTime endDateTime
    ) {
        final SortedMap<OffsetDateTime, BigDecimal> totalInvestments = getTotalInvestments(investments);
        return MathUtils.getWeightedAverage(totalInvestments, endDateTime);
    }

    private SortedMap<OffsetDateTime, BigDecimal> getTotalInvestments(
            final SortedMap<OffsetDateTime, BigDecimal> investments
    ) {
        final SortedMap<OffsetDateTime, BigDecimal> balances = new TreeMap<>();
        BigDecimal currentBalance = BigDecimal.ZERO;
        for (Map.Entry<OffsetDateTime, BigDecimal> entry : investments.entrySet()) {
            currentBalance = currentBalance.add(entry.getValue());
            balances.put(entry.getKey(), currentBalance);
        }
        return balances;
    }

    private double getRelativeYearProfit(final Interval interval, final double relativeProfit) {
        final BigDecimal partOfYear = BigDecimal.valueOf(interval.toDays() / DateUtils.DAYS_IN_YEAR);
        return BigDecimal.valueOf(relativeProfit).divide(partOfYear, RoundingMode.HALF_UP).doubleValue();
    }

    private List<SimulatedOperation> getOperations(final List<Operation> operations, final String ticker) {
        final List<SimulatedOperation> simulatedOperations = operations.stream()
                .map(operationMapper::map)
                .collect(Collectors.toList());
        simulatedOperations.forEach(operation -> operation.setTicker(ticker));
        return simulatedOperations;
    }

    private void saveSimulationResultsSafe(final String ticker, final List<SimulationResult> simulationResults) {
        try {
            log.debug("Saving simulation for ticker {} result to file", ticker);
            excelService.saveSimulationResults(ticker, simulationResults);
        } catch (Exception ex) {
            log.error("Failed to save simulation for ticker {} result to file", ticker, ex);
        }
    }

}