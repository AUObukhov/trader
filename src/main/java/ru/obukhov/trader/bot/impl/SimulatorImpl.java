package ru.obukhov.trader.bot.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.mapstruct.factory.Mappers;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.trader.bot.interfaces.BotFactory;
import ru.obukhov.trader.bot.interfaces.FakeBot;
import ru.obukhov.trader.bot.interfaces.Simulator;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.bot.model.StrategyConfig;
import ru.obukhov.trader.bot.strategy.TradingStrategy;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.CollectionsUtils;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.MathUtils;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.transform.OperationMapper;
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
            ExcelService excelService,
            BotFactory fakeBotFactory,
            TradingStrategyFactory strategyFactory,
            @Value("${simulation.thread-count:10}") Integer simulationThreadCount
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
            String ticker,
            BigDecimal initialBalance,
            BigDecimal balanceIncrement,
            CronExpression balanceIncrementCron,
            List<StrategyConfig> strategiesConfigs,
            Interval interval,
            boolean saveToFiles
    ) {
        log.info("Simulation for ticker = '{}' started", ticker);

        OffsetDateTime startTime = OffsetDateTime.now();
        DateUtils.assertDateTimeNotFuture(interval.getFrom(), startTime, "from");
        DateUtils.assertDateTimeNotFuture(interval.getTo(), startTime, "to");

        final Interval finiteInterval = interval.limitByNowIfNull();

        List<CompletableFuture<SimulationResult>> simulationFutures = strategiesConfigs.stream()
                .map(this::createFakeBot)
                .map(bot -> startSimulation(bot, ticker, initialBalance, balanceIncrement, balanceIncrementCron, finiteInterval))
                .collect(Collectors.toList());
        List<SimulationResult> simulationResults = simulationFutures.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparing(SimulationResult::getFinalTotalBalance).reversed())
                .collect(Collectors.toList());

        Duration simulationDuration = Duration.between(startTime, OffsetDateTime.now());
        String simulationDurationString = DurationFormatUtils.formatDurationHMS(simulationDuration.toMillis());
        log.info("Simulation for ticker = '{}' ended within {}", ticker, simulationDurationString);

        if (saveToFiles) {
            saveSimulationResultsSafe(ticker, simulationResults);
        }

        return simulationResults;
    }

    private FakeBot createFakeBot(StrategyConfig strategyConfig) {
        final TradingStrategy strategy = strategyFactory.createStrategy(strategyConfig);
        return (FakeBot) fakeBotFactory.createBot(strategy);
    }

    private CompletableFuture<SimulationResult> startSimulation(
            FakeBot bot,
            String ticker,
            BigDecimal initialBalance,
            BigDecimal balanceIncrement,
            CronExpression balanceIncrementCron,
            Interval interval
    ) {
        return CompletableFuture.supplyAsync(
                () -> simulateSafe(bot, ticker, initialBalance, balanceIncrement, balanceIncrementCron, interval),
                executor
        );
    }

    private SimulationResult simulateSafe(
            FakeBot bot,
            String ticker,
            BigDecimal initialBalance,
            BigDecimal balanceIncrement,
            CronExpression balanceIncrementCron,
            Interval interval
    ) {
        try {
            log.info("Simulation for '{}' with ticker = '{}' started", bot.getStrategyName(), ticker);
            SimulationResult result =
                    simulate(bot, ticker, initialBalance, balanceIncrement, balanceIncrementCron, interval);
            log.info("Simulation for '{}' with ticker = '{}' ended", bot.getStrategyName(), ticker);
            return result;
        } catch (Exception ex) {
            String message = String.format(
                    "Simulation for '%s' with ticker '%s' failed with error: %s",
                    bot.getStrategyName(), ticker, ex.getMessage()
            );
            log.error(message, ex);
            return SimulationResult.builder()
                    .botName(bot.getStrategyName())
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
            FakeBot bot,
            String ticker,
            BigDecimal initialBalance,
            BigDecimal balanceIncrement,
            CronExpression balanceIncrementCron,
            Interval interval
    ) {
        FakeTinkoffService fakeTinkoffService = bot.getFakeTinkoffService();

        MarketInstrument marketInstrument = fakeTinkoffService.searchMarketInstrument(ticker);
        if (marketInstrument == null) {
            throw new IllegalArgumentException("Not found instrument for ticker '" + ticker + "'");
        }

        fakeTinkoffService.init(interval.getFrom(), marketInstrument.getCurrency(), initialBalance);
        List<Candle> historicalCandles = new ArrayList<>();
        OffsetDateTime previousStartTime = null;

        do {
            DecisionData decisionData = bot.processTicker(ticker, previousStartTime);
            previousStartTime = getStartTime(decisionData);
            addLastCandle(historicalCandles, decisionData);

            moveToNextMinute(ticker, balanceIncrement, balanceIncrementCron, fakeTinkoffService);
        } while (fakeTinkoffService.getCurrentDateTime().isBefore(interval.getTo()));

        return createResult(bot, interval, ticker, historicalCandles);
    }

    private OffsetDateTime getStartTime(DecisionData decisionData) {
        return decisionData.getCurrentCandles().isEmpty()
                ? null
                : decisionData.getCurrentCandles().get(0).getTime();
    }

    private void moveToNextMinute(
            String ticker,
            BigDecimal balanceIncrement,
            CronExpression balanceIncrementCron,
            FakeTinkoffService fakeTinkoffService
    ) {
        if (balanceIncrement == null) {
            fakeTinkoffService.nextMinute();
        } else {
            OffsetDateTime previousDate = fakeTinkoffService.getCurrentDateTime();
            OffsetDateTime nextDate = fakeTinkoffService.nextMinute();

            int incrementsCount = DateUtils.getCronHitsBetweenDates(balanceIncrementCron, previousDate, nextDate);
            if (incrementsCount > 0) {
                BigDecimal totalBalanceIncrement = DecimalUtils.multiply(balanceIncrement, incrementsCount);
                Currency currency = getCurrency(fakeTinkoffService, ticker);
                BigDecimal currentBalance = fakeTinkoffService.getCurrentBalance(currency);
                log.debug("Incrementing balance {} by {}", currentBalance, totalBalanceIncrement);
                fakeTinkoffService.incrementBalance(currency, totalBalanceIncrement);
            }
        }
    }

    private void addLastCandle(List<Candle> historicalCandles, DecisionData decisionData) {
        if (!decisionData.getCurrentCandles().isEmpty()) {
            List<Candle> currentCandles = decisionData.getCurrentCandles();
            Candle candle = currentCandles.get(currentCandles.size() - 1);
            if (candle != null) {
                historicalCandles.add(candle);
            }
        }
    }

    private SimulationResult createResult(
            FakeBot bot,
            Interval interval,
            String ticker,
            List<Candle> candles
    ) {
        FakeTinkoffService fakeTinkoffService = bot.getFakeTinkoffService();

        List<SimulatedPosition> positions = getPositions(fakeTinkoffService.getPortfolioPositions(), candles);
        Currency currency = getCurrency(fakeTinkoffService, ticker);

        SortedMap<OffsetDateTime, BigDecimal> investments = fakeTinkoffService.getInvestments(currency);

        BigDecimal initialBalance = investments.get(investments.firstKey());
        BigDecimal currentBalance = fakeTinkoffService.getCurrentBalance(currency);
        BigDecimal totalBalance = getTotalBalance(currentBalance, positions);

        BigDecimal totalInvestment = investments.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal weightedAverageInvestment = getWeightedAverage(investments, interval.getTo());

        BigDecimal absoluteProfit = totalBalance.subtract(totalInvestment);
        double relativeProfit = DecimalUtils.divide(absoluteProfit, weightedAverageInvestment).doubleValue();
        double relativeYearProfit = getRelativeYearProfit(interval, relativeProfit);
        List<Operation> operations = fakeTinkoffService.getOperations(interval, ticker);

        return SimulationResult.builder()
                .botName(bot.getStrategyName())
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

    private Currency getCurrency(FakeTinkoffService fakeTinkoffService, String ticker) {
        return fakeTinkoffService.searchMarketInstrument(ticker).getCurrency();
    }

    private List<SimulatedPosition> getPositions(
            Collection<PortfolioPosition> portfolioPositions,
            List<Candle> candles
    ) {
        Candle lastCandle = CollectionsUtils.getLast(candles);
        BigDecimal currentPrice = lastCandle == null ? null : lastCandle.getClosePrice();

        return portfolioPositions.stream()
                .map(portfolioPosition -> createSimulatedPosition(portfolioPosition, currentPrice))
                .collect(Collectors.toList());
    }

    private SimulatedPosition createSimulatedPosition(PortfolioPosition portfolioPosition, BigDecimal currentPrice) {
        return new SimulatedPosition(portfolioPosition.getTicker(), currentPrice, portfolioPosition.getLotsCount());
    }

    private BigDecimal getTotalBalance(BigDecimal balance, List<SimulatedPosition> positions) {
        return positions.stream()
                .map(position -> DecimalUtils.multiply(position.getPrice(), position.getQuantity()))
                .reduce(balance, BigDecimal::add);
    }

    private BigDecimal getWeightedAverage(
            SortedMap<OffsetDateTime, BigDecimal> investments,
            OffsetDateTime endDateTime
    ) {
        SortedMap<OffsetDateTime, BigDecimal> totalInvestments = getTotalInvestments(investments);
        return MathUtils.getWeightedAverage(totalInvestments, endDateTime);
    }

    private SortedMap<OffsetDateTime, BigDecimal> getTotalInvestments(
            SortedMap<OffsetDateTime, BigDecimal> investments
    ) {
        SortedMap<OffsetDateTime, BigDecimal> balances = new TreeMap<>();
        BigDecimal currentBalance = BigDecimal.ZERO;
        for (Map.Entry<OffsetDateTime, BigDecimal> entry : investments.entrySet()) {
            currentBalance = currentBalance.add(entry.getValue());
            balances.put(entry.getKey(), currentBalance);
        }
        return balances;
    }

    private double getRelativeYearProfit(Interval interval, double relativeProfit) {
        BigDecimal partOfYear = BigDecimal.valueOf(interval.toDays() / DateUtils.DAYS_IN_YEAR);
        return BigDecimal.valueOf(relativeProfit).divide(partOfYear, RoundingMode.HALF_UP).doubleValue();
    }

    private List<SimulatedOperation> getOperations(List<Operation> operations, String ticker) {
        List<SimulatedOperation> simulatedOperations = operations.stream()
                .map(operationMapper::map)
                .collect(Collectors.toList());
        simulatedOperations.forEach(operation -> operation.setTicker(ticker));
        return simulatedOperations;
    }

    private void saveSimulationResultsSafe(String ticker, List<SimulationResult> simulationResults) {
        try {
            log.debug("Saving simulation for ticker {} result to file", ticker);
            excelService.saveSimulationResults(ticker, simulationResults);
        } catch (Exception ex) {
            log.error("Failed to save simulation for ticker {} result to file", ticker, ex);
        }
    }

}