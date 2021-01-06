package ru.obukhov.investor.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.investor.bot.interfaces.Simulator;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.model.Interval;
import ru.obukhov.investor.model.transform.SimulationResultMapper;
import ru.obukhov.investor.web.model.SimulateRequest;
import ru.obukhov.investor.web.model.SimulateResponse;
import ru.obukhov.investor.web.model.SimulationResult;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/investor/bot")
@RequiredArgsConstructor
public class BotController {

    private final Simulator simulator;
    private final TradingProperties tradingProperties;
    private final SimulationResultMapper simulationResultMapper = Mappers.getMapper(SimulationResultMapper.class);

    @PostMapping("/simulate")
    public SimulateResponse simulate(@Valid @RequestBody SimulateRequest request) {

        Interval interval = Interval.of(request.getFrom(), request.getTo());

        SimulationResult result = simulator.simulate(request.getTicker(), request.getBalance(), interval);

        return simulationResultMapper.map(result);

    }

    @PostMapping("/enable")
    public void enable() {
        tradingProperties.setBotEnabled(true);
    }

    @PostMapping("/disable")
    public void disable() {
        tradingProperties.setBotEnabled(false);
    }
}