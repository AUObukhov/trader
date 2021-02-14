package ru.obukhov.investor.bot.impl;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import ru.obukhov.investor.bot.interfaces.FakeBot;
import ru.obukhov.investor.bot.interfaces.Simulator;
import ru.obukhov.investor.bot.model.DecisionData;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.Interval;
import ru.obukhov.investor.model.PortfolioPosition;
import ru.obukhov.investor.model.transform.OperationMapper;
import ru.obukhov.investor.service.impl.FakeTinkoffService;
import ru.obukhov.investor.service.interfaces.ExcelService;
import ru.obukhov.investor.util.DateUtils;
import ru.obukhov.investor.util.MathUtils;
import ru.obukhov.investor.web.model.SimulatedOperation;
import ru.obukhov.investor.web.model.SimulatedPosition;
import ru.obukhov.investor.web.model.SimulationResult;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simulates trading by bot
 */
@Slf4j
@RequiredArgsConstructor
public class SimulatorImpl implements Simulator {

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);

    private final Collection<FakeBot> bots;
    private final FakeTinkoffService fakeTinkoffService;
    private final ExcelService excelService;

    /**
     * @param ticker   simulated ticker
     * @param balance  balance before simulation
     * @param interval simulation interval
     * @return balance after simulation
     */
    @Override
    public List<SimulationResult> simulate(String ticker, BigDecimal balance, Interval interval) {

        log.info("Simulation for ticker = '{}' started", ticker);

        OffsetDateTime now = OffsetDateTime.now();
        DateUtils.assertDateTimeNotFuture(interval.getFrom(), now, "from");
        DateUtils.assertDateTimeNotFuture(interval.getTo(), now, "to");

        final Interval finiteInterval = interval.limitByNowIfNull();

        List<SimulationResult> simulationResults = bots.stream()
                .map(bot -> simulate(bot, ticker, balance, finiteInterval))
                .sorted(Comparator.comparing(SimulationResult::getTotalBalance).reversed())
                .collect(Collectors.toList());

        log.info("Simulation for ticker = '{}' ended", ticker);

        excelService.saveSimulationResults(simulationResults);

        return simulationResults;
    }

    private SimulationResult simulate(FakeBot bot, String ticker, BigDecimal balance, Interval interval) {
        log.info("Simulation for ticker = '{}' on bot '{}' started", ticker, bot.getName());

        fakeTinkoffService.init(interval.getFrom(), balance);
        List<Candle> candles = new ArrayList<>();

        do {

            DecisionData decisionData = bot.processTicker(ticker);
            addLastCandle(candles, decisionData);

            fakeTinkoffService.nextMinute();

        } while (fakeTinkoffService.getCurrentDateTime().isBefore(interval.getTo()));

        log.info("Simulation for ticker = '{}' on bot '{}' ended", ticker, bot.getName());

        return createResult(bot.getName(), balance, interval, ticker, candles);
    }

    private void addLastCandle(List<Candle> candles, DecisionData decisionData) {
        List<Candle> currentCandles = decisionData.getCurrentCandles();
        Candle candle = Iterables.getLast(currentCandles);
        if (candle != null) {
            candles.add(candle);
        }
    }

    private SimulationResult createResult(String botName,
                                          BigDecimal initialBalance,
                                          Interval interval,
                                          String ticker,
                                          List<Candle> candles) {

        BigDecimal currentPrice = Iterables.getLast(candles).getClosePrice();
        List<SimulatedPosition> positions = getPositions(currentPrice);
        BigDecimal totalBalance = getTotalBalance(positions);
        BigDecimal absoluteProfit = totalBalance.subtract(initialBalance);
        double relativeProfit = MathUtils.divide(absoluteProfit, initialBalance).doubleValue();
        double relativeYearProfit = relativeProfit / (interval.toDuration().toDays() / DateUtils.DAYS_IN_YEAR);


        return SimulationResult.builder()
                .botName(botName)
                .interval(interval)
                .initialBalance(initialBalance)
                .totalBalance(totalBalance)
                .currencyBalance(fakeTinkoffService.getBalance())
                .absoluteProfit(absoluteProfit)
                .relativeProfit(relativeProfit)
                .relativeYearProfit(relativeYearProfit)
                .positions(positions)
                .operations(getOperations(interval, ticker))
                .candles(candles)
                .build();
    }

    private List<SimulatedPosition> getPositions(BigDecimal currentPrice) {
        return fakeTinkoffService.getPortfolioPositions().stream()
                .map(portfolioPosition -> createSimulatedPosition(portfolioPosition, currentPrice))
                .collect(Collectors.toList());
    }

    private SimulatedPosition createSimulatedPosition(PortfolioPosition portfolioPosition, BigDecimal currentPrice) {
        return new SimulatedPosition(portfolioPosition.getTicker(), currentPrice, portfolioPosition.getLotsCount());
    }

    private BigDecimal getTotalBalance(List<SimulatedPosition> positions) {
        return positions.stream()
                .map(position -> MathUtils.multiply(position.getPrice(), position.getQuantity()))
                .reduce(fakeTinkoffService.getBalance(), BigDecimal::add);
    }

    private List<SimulatedOperation> getOperations(Interval interval, String ticker) {
        List<SimulatedOperation> operations = fakeTinkoffService.getOperations(interval, ticker).stream()
                .map(operationMapper::map)
                .collect(Collectors.toList());
        operations.forEach(operation -> operation.setTicker(ticker));
        return operations;
    }

}