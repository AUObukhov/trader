package ru.obukhov.investor.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.investor.bot.interfaces.Simulator;
import ru.obukhov.investor.common.model.Interval;
import ru.obukhov.investor.common.util.DateUtils;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.web.model.exchange.SimulateRequest;
import ru.obukhov.investor.web.model.exchange.SimulateResponse;
import ru.obukhov.investor.web.model.pojo.SimulationResult;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/investor/bot")
@RequiredArgsConstructor
public class BotController {

    private final Simulator simulator;
    private final TradingProperties tradingProperties;

    @PostMapping("/simulate")
    public SimulateResponse simulate(@Valid @RequestBody SimulateRequest request) {

        Interval interval = DateUtils.getIntervalWithDefaultOffsets(request.getFrom(), request.getTo());
        boolean saveToFile = BooleanUtils.isTrue(request.getSaveToFile());

        Collection<SimulationResult> results = simulator.simulate(
                request.getTicker(),
                request.getBalance(),
                interval,
                saveToFile);

        return new SimulateResponse(results);

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