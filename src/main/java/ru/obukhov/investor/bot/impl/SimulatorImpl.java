package ru.obukhov.investor.bot.impl;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.util.Assert;
import ru.obukhov.investor.bot.interfaces.FakeBot;
import ru.obukhov.investor.bot.interfaces.Simulator;
import ru.obukhov.investor.bot.model.DecisionData;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.Interval;
import ru.obukhov.investor.model.transform.OperationMapper;
import ru.obukhov.investor.model.transform.PositionMapper;
import ru.obukhov.investor.service.impl.FakeTinkoffService;
import ru.obukhov.investor.service.interfaces.ExcelService;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.util.DateUtils;
import ru.obukhov.investor.util.MathUtils;
import ru.obukhov.investor.web.model.SimulatedOperation;
import ru.obukhov.investor.web.model.SimulatedPosition;
import ru.obukhov.investor.web.model.SimulationResult;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simulates trading by bot
 */
@Slf4j
@RequiredArgsConstructor
public class SimulatorImpl implements Simulator {

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);
    private final PositionMapper positionMapper = Mappers.getMapper(PositionMapper.class);

    private final Collection<FakeBot> bots;
    private final MarketService fakeMarketService;
    private final FakeTinkoffService fakeTinkoffService;
    private final ExcelService excelService;
    private final TradingProperties tradingProperties;

    /**
     * @param ticker   simulated ticker
     * @param interval simulation interval
     * @return balance after simulation
     */
    @Override
    public List<SimulationResult> simulate(String ticker, Interval interval) {

        log.info("Simulation for ticker = '" + ticker + "' started");

        OffsetDateTime now = OffsetDateTime.now();
        Assert.isTrue(!interval.getFrom().isAfter(now), "'from' can't be in future");
        Assert.isTrue(interval.getTo() == null || !interval.getTo().isAfter(now),
                "'to' can't be in future");

        final Interval finiteInterval = interval.limitByNowIfNull();

        List<SimulationResult> simulationResults = bots.stream()
                .map(bot -> simulate(bot, ticker, finiteInterval))
                .collect(Collectors.toList());

        log.info("Simulation for ticker = '" + ticker + "' ended");

        excelService.saveSimulationResults(simulationResults);

        return simulationResults;
    }

    private SimulationResult simulate(FakeBot bot, String ticker, Interval interval) {
        log.info("Simulation for ticker = '" + ticker + "' on bot '" + bot.getName() + "' started");

        BigDecimal initialBalance = initSimulation(ticker, interval);
        List<Candle> candles = new ArrayList<>();

        do {

            DecisionData decisionData = bot.processTicker(ticker);
            addLastCandle(candles, decisionData);

            fakeTinkoffService.nextMinute();

        } while (fakeTinkoffService.getCurrentDateTime().isBefore(interval.getTo()));

        log.info("Simulation for ticker = '" + ticker + "' on bot '" + bot.getName() + "' ended");

        return createResult(bot.getName(), initialBalance, interval, ticker, candles);
    }

    private void addLastCandle(List<Candle> candles, DecisionData decisionData) {
        List<Candle> currentCandles = decisionData.getCurrentCandles();
        Candle candle = Iterables.getLast(currentCandles);
        if (candle != null) {
            candles.add(candle);
        }
    }

    private BigDecimal initSimulation(String ticker, Interval interval) {
        fakeTinkoffService.clear();
        fakeTinkoffService.initCurrentDateTime(interval.getFrom());

        BigDecimal initialBalance = getInitialBalance(ticker);
        fakeTinkoffService.setBalance(initialBalance);

        return initialBalance;
    }

    private BigDecimal getInitialBalance(String ticker) {
        BigDecimal currentPrice = fakeMarketService.getLastCandles(ticker, 1).stream()
                .map(Candle::getClosePrice)
                .findFirst()
                .orElseThrow();
        return MathUtils.addFraction(currentPrice, tradingProperties.getCommission());
    }

    private SimulationResult createResult(String botName,
                                          BigDecimal initialBalance,
                                          Interval interval,
                                          String ticker,
                                          List<Candle> candles) {

        BigDecimal totalBalance = getTotalBalance();
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
                .positions(getPositions())
                .operations(getOperations(interval, ticker))
                .candles(candles)
                .build();
    }

    private List<SimulatedPosition> getPositions() {
        return fakeTinkoffService.getPortfolioPositions().stream()
                .map(positionMapper::map)
                .collect(Collectors.toList());
    }

    private BigDecimal getTotalBalance() {
        return fakeTinkoffService.getPortfolioPositions().stream()
                .map(position -> position.balance)
                .reduce(fakeTinkoffService.getBalance(), BigDecimal::add);
    }

    private List<SimulatedOperation> getOperations(Interval interval, String ticker) {
        return fakeTinkoffService.getOperations(interval, ticker).stream()
                .map(operationMapper::map)
                .collect(Collectors.toList());
    }

}