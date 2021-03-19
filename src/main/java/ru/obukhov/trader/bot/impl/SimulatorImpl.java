package ru.obukhov.trader.bot.impl;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.bot.interfaces.BotFactory;
import ru.obukhov.trader.bot.interfaces.FakeBot;
import ru.obukhov.trader.bot.interfaces.Simulator;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.MathUtils;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.transform.OperationMapper;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;
import ru.obukhov.trader.web.model.pojo.SimulatedPosition;
import ru.obukhov.trader.web.model.pojo.SimulationResult;
import ru.obukhov.trader.web.model.pojo.SimulationUnit;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.operations.Operation;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

/**
 * Simulates trading by bot
 */
@Slf4j
@Service
public class SimulatorImpl implements Simulator {

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);

    private final ExcelService excelService;
    private final BotFactory fakeBotFactory;
    private final ExecutorService executor;

    public SimulatorImpl(ExcelService excelService,
                         BotFactory fakeBotFactory,
                         @Value("${simulation.thread-count:10}") Integer simulationThreadCount) {

        this.excelService = excelService;
        this.fakeBotFactory = fakeBotFactory;
        this.executor = createExecutor(simulationThreadCount);

    }

    private ExecutorService createExecutor(Integer simulationThreadCount) {
        ThreadFactory simulationThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("simulation-thread-%d")
                .build();
        return Executors.newFixedThreadPool(simulationThreadCount, simulationThreadFactory);
    }

    /**
     * @param simulationUnits list of simulated tickers and corresponding initial balances
     * @param interval        all simulations interval
     * @param saveToFiles     flag to save simulations results to file
     * @return map of simulations results by tickers
     */
    @Override
    public Map<String, List<SimulationResult>> simulate(List<SimulationUnit> simulationUnits,
                                                        Interval interval,
                                                        boolean saveToFiles) {

        Map<String, CompletableFuture<List<SimulationResult>>> tickersToSimulationResults = simulationUnits.stream()
                .collect(Collectors.toMap(
                        SimulationUnit::getTicker,
                        simulationUnit -> startSimulations(simulationUnit, interval, saveToFiles)
                ));

        return tickersToSimulationResults.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().join()
                ));
    }

    private CompletableFuture<List<SimulationResult>> startSimulations(SimulationUnit simulationUnit,
                                                                       Interval interval,
                                                                       boolean saveToFile) {
        return CompletableFuture.supplyAsync(() -> simulate(simulationUnit, interval, saveToFile), executor);
    }

    private List<SimulationResult> simulate(SimulationUnit simulationUnit, Interval interval, boolean saveToFile) {

        log.info("Simulation for ticker = '{}' started", simulationUnit.getTicker());

        OffsetDateTime startTime = OffsetDateTime.now();
        DateUtils.assertDateTimeNotFuture(interval.getFrom(), startTime, "from");
        DateUtils.assertDateTimeNotFuture(interval.getTo(), startTime, "to");

        final Interval finiteInterval = interval.limitByNowIfNull();

        List<CompletableFuture<SimulationResult>> simulationFutures = createFakeBots().stream()
                .map(bot -> startSimulation(bot, simulationUnit, finiteInterval))
                .collect(Collectors.toList());
        List<SimulationResult> simulationResults = simulationFutures.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparing(SimulationResult::getFinalTotalBalance).reversed())
                .collect(Collectors.toList());

        Duration simulationDuration = Duration.between(startTime, OffsetDateTime.now());
        String simulationDurationString = DurationFormatUtils.formatDurationHMS(simulationDuration.toMillis());
        log.info("Simulation for ticker = '{}' ended within {}", simulationUnit.getTicker(), simulationDurationString);

        if (saveToFile) {
            saveSimulationResultsSafe(simulationUnit.getTicker(), simulationResults);
        }

        return simulationResults;
    }

    @SuppressWarnings("unchecked")
    private Set<FakeBot> createFakeBots() {
        return (Set<FakeBot>) (Set<?>) fakeBotFactory.createBots();
    }

    private CompletableFuture<SimulationResult> startSimulation(FakeBot bot,
                                                                SimulationUnit simulationUnit,
                                                                Interval interval) {
        return CompletableFuture.supplyAsync(() -> simulateSafe(bot, simulationUnit, interval), executor);
    }

    private SimulationResult simulateSafe(FakeBot bot, SimulationUnit simulationUnit, Interval interval) {
        try {
            log.info("Simulation for bot '{}' with ticker = '{}' started", bot.getName(), simulationUnit.getTicker());
            SimulationResult result = simulate(bot, simulationUnit, interval);
            log.info("Simulation for bot '{}' with ticker = '{}' ended", bot.getName(), simulationUnit.getTicker());
            return result;
        } catch (Exception ex) {
            String message = String.format("Simulation for bot '%s' with ticker '%s' failed",
                    bot.getName(), simulationUnit.getTicker());
            log.error(message, ex);
            return SimulationResult.builder().error(message).build();
        }
    }

    private SimulationResult simulate(FakeBot bot, SimulationUnit simulationUnit, Interval interval) {

        FakeTinkoffService fakeTinkoffService = bot.getFakeTinkoffService();
        Currency currency = getCurrency(fakeTinkoffService, simulationUnit.getTicker());
        fakeTinkoffService.init(interval.getFrom(), currency, simulationUnit.getInitialBalance());
        List<Candle> candles = new ArrayList<>();

        do {

            DecisionData decisionData = bot.processTicker(simulationUnit.getTicker());
            addLastCandle(candles, decisionData);

            moveToNextMinute(simulationUnit, fakeTinkoffService);

        } while (fakeTinkoffService.getCurrentDateTime().isBefore(interval.getTo()));

        return createResult(bot, interval, simulationUnit.getTicker(), candles);
    }

    private void moveToNextMinute(SimulationUnit simulationUnit, FakeTinkoffService fakeTinkoffService) {
        if (simulationUnit.isBalanceIncremented()) {
            OffsetDateTime previousDate = fakeTinkoffService.getCurrentDateTime();
            OffsetDateTime nextDate = fakeTinkoffService.nextMinute();

            int incrementsCount =
                    DateUtils.getCronHitsBetweenDates(simulationUnit.getBalanceIncrementCron(), previousDate, nextDate);
            if (incrementsCount > 0) {
                BigDecimal balanceIncrement = MathUtils.multiply(simulationUnit.getBalanceIncrement(), incrementsCount);
                Currency currency = getCurrency(fakeTinkoffService, simulationUnit.getTicker());
                BigDecimal currentBalance = fakeTinkoffService.getCurrentBalance(currency);
                log.debug("Incrementing balance {} by {}", currentBalance, balanceIncrement);
                fakeTinkoffService.incrementBalance(currency, balanceIncrement);
            }
        } else {
            fakeTinkoffService.nextMinute();
        }
    }

    private void addLastCandle(List<Candle> candles, DecisionData decisionData) {
        List<Candle> currentCandles = decisionData.getCurrentCandles();
        Candle candle = Iterables.getLast(currentCandles);
        if (candle != null) {
            candles.add(candle);
        }
    }

    private SimulationResult createResult(FakeBot bot,
                                          Interval interval,
                                          String ticker,
                                          List<Candle> candles) {

        FakeTinkoffService fakeTinkoffService = bot.getFakeTinkoffService();

        BigDecimal currentPrice = Iterables.getLast(candles).getClosePrice();
        List<SimulatedPosition> positions = getPositions(fakeTinkoffService.getPortfolioPositions(), currentPrice);
        Currency currency = getCurrency(fakeTinkoffService, ticker);

        SortedMap<OffsetDateTime, BigDecimal> investments = fakeTinkoffService.getInvestments(currency);

        BigDecimal initialBalance = investments.get(investments.firstKey());
        BigDecimal currentBalance = fakeTinkoffService.getCurrentBalance(currency);
        BigDecimal totalBalance = getTotalBalance(currentBalance, positions);

        BigDecimal totalInvestment = investments.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal weightedAverageInvestment = getWeightedAverage(investments, interval.getTo());

        BigDecimal absoluteProfit = totalBalance.subtract(totalInvestment);
        double relativeProfit = MathUtils.divide(absoluteProfit, weightedAverageInvestment).doubleValue();
        double relativeYearProfit = relativeProfit / (interval.toDuration().toDays() / DateUtils.DAYS_IN_YEAR);
        List<Operation> operations = fakeTinkoffService.getOperations(interval, ticker);

        return SimulationResult.builder()
                .botName(bot.getName())
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
        return fakeTinkoffService.searchMarketInstrument(ticker).currency;
    }

    private List<SimulatedPosition> getPositions(Collection<PortfolioPosition> portfolioPositions,
                                                 BigDecimal currentPrice) {
        return portfolioPositions.stream()
                .map(portfolioPosition -> createSimulatedPosition(portfolioPosition, currentPrice))
                .collect(Collectors.toList());
    }

    private SimulatedPosition createSimulatedPosition(PortfolioPosition portfolioPosition, BigDecimal currentPrice) {
        return new SimulatedPosition(portfolioPosition.getTicker(), currentPrice, portfolioPosition.getLotsCount());
    }

    private BigDecimal getTotalBalance(BigDecimal balance, List<SimulatedPosition> positions) {
        return positions.stream()
                .map(position -> MathUtils.multiply(position.getPrice(), position.getQuantity()))
                .reduce(balance, BigDecimal::add);
    }

    private BigDecimal getWeightedAverage(SortedMap<OffsetDateTime, BigDecimal> investments,
                                          OffsetDateTime endDateTime) {
        SortedMap<OffsetDateTime, BigDecimal> totalInvestments = getTotalInvestments(investments);
        return MathUtils.getWeightedAverage(totalInvestments, endDateTime);
    }

    private SortedMap<OffsetDateTime, BigDecimal> getTotalInvestments(SortedMap<OffsetDateTime, BigDecimal> investments) {
        SortedMap<OffsetDateTime, BigDecimal> balances = new TreeMap<>();
        BigDecimal currentBalance = BigDecimal.ZERO;
        for (Map.Entry<OffsetDateTime, BigDecimal> entry : investments.entrySet()) {
            currentBalance = currentBalance.add(entry.getValue());
            balances.put(entry.getKey(), currentBalance);
        }
        return balances;
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