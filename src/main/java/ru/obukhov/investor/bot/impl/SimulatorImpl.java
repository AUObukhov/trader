package ru.obukhov.investor.bot.impl;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.bot.interfaces.Bot;
import ru.obukhov.investor.bot.interfaces.BotFactory;
import ru.obukhov.investor.bot.interfaces.FakeBot;
import ru.obukhov.investor.bot.interfaces.Simulator;
import ru.obukhov.investor.bot.model.DecisionData;
import ru.obukhov.investor.common.model.Interval;
import ru.obukhov.investor.common.service.interfaces.ExcelService;
import ru.obukhov.investor.common.util.DateUtils;
import ru.obukhov.investor.common.util.MathUtils;
import ru.obukhov.investor.market.impl.FakeTinkoffService;
import ru.obukhov.investor.market.model.Candle;
import ru.obukhov.investor.market.model.PortfolioPosition;
import ru.obukhov.investor.market.model.transform.OperationMapper;
import ru.obukhov.investor.web.model.pojo.SimulatedOperation;
import ru.obukhov.investor.web.model.pojo.SimulatedPosition;
import ru.obukhov.investor.web.model.pojo.SimulationResult;
import ru.obukhov.investor.web.model.pojo.SimulationUnit;
import ru.tinkoff.invest.openapi.models.operations.Operation;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
@RequiredArgsConstructor
public class SimulatorImpl implements Simulator {

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);

    private final ExcelService excelService;
    private final BotFactory fakeBotFactory;
    private final ThreadFactory simulationThreadFactory =
            new ThreadFactoryBuilder().setNameFormat("simulation-thread-%d").build();
    private final ExecutorService executor = Executors.newFixedThreadPool(10, simulationThreadFactory);

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
        return CompletableFuture.supplyAsync(
                () -> simulate(simulationUnit.getTicker(), simulationUnit.getBalance(), interval, saveToFile),
                executor);
    }

    private List<SimulationResult> simulate(String ticker, BigDecimal balance, Interval interval, boolean saveToFile) {

        log.info("Simulation for ticker = '{}' started", ticker);

        OffsetDateTime startTime = OffsetDateTime.now();
        DateUtils.assertDateTimeNotFuture(interval.getFrom(), startTime, "from");
        DateUtils.assertDateTimeNotFuture(interval.getTo(), startTime, "to");

        final Interval finiteInterval = interval.limitByNowIfNull();

        List<CompletableFuture<SimulationResult>> simulationFutures = createFakeBots().stream()
                .map(bot -> startSimulation(bot, ticker, balance, finiteInterval))
                .collect(Collectors.toList());
        List<SimulationResult> simulationResults = simulationFutures.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparing(SimulationResult::getTotalBalance).reversed())
                .collect(Collectors.toList());

        Duration simulationDuration = Duration.between(startTime, OffsetDateTime.now());
        String simulationDurationString = DurationFormatUtils.formatDurationHMS(simulationDuration.toMillis());
        log.info("Simulation for ticker = '{}' ended within {}", ticker, simulationDurationString);

        if (saveToFile) {
            saveSimulationResultsSafe(ticker, simulationResults);
        }

        return simulationResults;
    }

    private Set<FakeBot> createFakeBots() {
        Set<FakeBot> fakeBots = new HashSet<>();
        fakeBots.add((FakeBot) fakeBotFactory.createConservativeBot());
        fakeBots.add((FakeBot) fakeBotFactory.createDumbBot());
        for (Bot bot : fakeBotFactory.createTrendReversalBots()) {
            fakeBots.add((FakeBot) bot);
        }
        return fakeBots;
    }

    private CompletableFuture<SimulationResult> startSimulation(FakeBot bot,
                                                                String ticker,
                                                                BigDecimal balance,
                                                                Interval interval) {
        return CompletableFuture.supplyAsync(() -> simulateSafe(bot, ticker, balance, interval), executor);
    }

    private SimulationResult simulateSafe(FakeBot bot, String ticker, BigDecimal balance, Interval interval) {
        try {
            log.info("Simulation for bot '{}' with ticker = '{}' started", bot.getName(), ticker);
            SimulationResult result = simulate(bot, ticker, balance, interval);
            log.info("Simulation for bot '{}' with ticker = '{}' ended", bot.getName(), ticker);
            return result;
        } catch (Exception ex) {
            String message = String.format("Simulation for bot '%s' with ticker '%s' failed", bot.getName(), ticker);
            log.error(message, ex);
            return SimulationResult.builder().error(message).build();
        }
    }

    private SimulationResult simulate(FakeBot bot, String ticker, BigDecimal balance, Interval interval) {

        FakeTinkoffService fakeTinkoffService = bot.getFakeTinkoffService();
        fakeTinkoffService.init(interval.getFrom(), balance);
        List<Candle> candles = new ArrayList<>();

        do {

            DecisionData decisionData = bot.processTicker(ticker);
            addLastCandle(candles, decisionData);

            fakeTinkoffService.nextMinute();

        } while (fakeTinkoffService.getCurrentDateTime().isBefore(interval.getTo()));

        return createResult(bot, balance, interval, ticker, candles);
    }

    private void addLastCandle(List<Candle> candles, DecisionData decisionData) {
        List<Candle> currentCandles = decisionData.getCurrentCandles();
        Candle candle = Iterables.getLast(currentCandles);
        if (candle != null) {
            candles.add(candle);
        }
    }

    private SimulationResult createResult(FakeBot bot,
                                          BigDecimal initialBalance,
                                          Interval interval,
                                          String ticker,
                                          List<Candle> candles) {

        FakeTinkoffService fakeTinkoffService = bot.getFakeTinkoffService();

        BigDecimal currentPrice = Iterables.getLast(candles).getClosePrice();
        List<SimulatedPosition> positions = getPositions(fakeTinkoffService.getPortfolioPositions(), currentPrice);
        BigDecimal totalBalance = getTotalBalance(fakeTinkoffService.getBalance(), positions);
        BigDecimal absoluteProfit = totalBalance.subtract(initialBalance);
        double relativeProfit = MathUtils.divide(absoluteProfit, initialBalance).doubleValue();
        double relativeYearProfit = relativeProfit / (interval.toDuration().toDays() / DateUtils.DAYS_IN_YEAR);
        List<Operation> operations = fakeTinkoffService.getOperations(interval, ticker);

        return SimulationResult.builder()
                .botName(bot.getName())
                .interval(interval)
                .initialBalance(initialBalance)
                .totalBalance(totalBalance)
                .currencyBalance(fakeTinkoffService.getBalance())
                .absoluteProfit(absoluteProfit)
                .relativeProfit(relativeProfit)
                .relativeYearProfit(relativeYearProfit)
                .positions(positions)
                .operations(getOperations(operations, ticker))
                .candles(candles)
                .build();
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