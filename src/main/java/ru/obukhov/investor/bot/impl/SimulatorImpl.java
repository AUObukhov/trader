package ru.obukhov.investor.bot.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import ru.obukhov.investor.bot.interfaces.FakeBot;
import ru.obukhov.investor.bot.interfaces.Simulator;
import ru.obukhov.investor.model.Interval;
import ru.obukhov.investor.model.transform.OperationMapper;
import ru.obukhov.investor.model.transform.PositionMapper;
import ru.obukhov.investor.service.impl.FakeTinkoffService;
import ru.obukhov.investor.service.interfaces.ExcelService;
import ru.obukhov.investor.web.model.SimulatedOperation;
import ru.obukhov.investor.web.model.SimulatedPosition;
import ru.obukhov.investor.web.model.SimulationResult;

import java.math.BigDecimal;
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

        log.info("Simulation for ticker = '" + ticker + "' started");

        final Interval finiteInterval = interval.limitByNowIfNull();

        List<SimulationResult> simulationResults = bots.stream()
                .map(bot -> simulate(bot, ticker, balance, finiteInterval))
                .collect(Collectors.toList());

        log.info("Simulation for ticker = '" + ticker + "' ended");

        excelService.saveSimulationResults(simulationResults);

        return simulationResults;
    }

    public SimulationResult simulate(FakeBot bot, String ticker, BigDecimal balance, Interval interval) {
        log.info("Simulation for ticker = '" + ticker + "' on bot '" + bot.getName() + "' started");

        fakeTinkoffService.init(interval.getFrom(), balance);

        do {

            bot.processTicker(ticker);

            fakeTinkoffService.nextMinute();

        } while (fakeTinkoffService.getCurrentDateTime().isBefore(interval.getTo()));

        log.info("Simulation for ticker = '" + ticker + "' on bot '" + bot.getName() + "' ended");

        return createResult(bot.getName(), interval, ticker);
    }


    private SimulationResult createResult(String botName, Interval interval, String ticker) {
        return SimulationResult.builder()
                .botName(botName)
                .currencyBalance(fakeTinkoffService.getBalance())
                .totalBalance(getFullBalance())
                .positions(getPositions())
                .operations(getOperations(interval, ticker))
                .build();
    }

    private List<SimulatedPosition> getPositions() {
        return fakeTinkoffService.getPortfolioPositions().stream()
                .map(positionMapper::map)
                .collect(Collectors.toList());
    }

    private BigDecimal getFullBalance() {
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