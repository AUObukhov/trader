package ru.obukhov.investor.bot.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import ru.obukhov.investor.bot.interfaces.Bot;
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
import java.time.OffsetDateTime;
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

    private final Bot bot;
    private final FakeTinkoffService fakeTinkoffService;
    private final ExcelService excelService;

    /**
     * @param ticker   simulated ticker
     * @param balance  balance before simulation
     * @param interval simulation interval
     * @return balance after simulation
     */
    @Override
    public SimulationResult simulate(String ticker, BigDecimal balance, Interval interval) {

        log.info("Simulation for ticker = '" + ticker + "' started");

        OffsetDateTime innerTo = interval.getTo() == null ? OffsetDateTime.now() : interval.getTo();
        Interval innerInterval = Interval.of(interval.getFrom(), innerTo);

        fakeTinkoffService.init(interval.getFrom(), balance);

        do {

            bot.processTicker(ticker);

            fakeTinkoffService.nextMinute();
        } while (fakeTinkoffService.getCurrentDateTime().isBefore(innerTo));

        log.info("Simulation for ticker = '" + ticker + "' ended");

        SimulationResult result = createResult(innerInterval, ticker);

        excelService.saveSimulationResult(result);

        return result;
    }

    private SimulationResult createResult(Interval interval, String ticker) {
        return SimulationResult.builder()
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