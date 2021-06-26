package ru.obukhov.trader.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.properties.ScheduledBotProperties;
import ru.obukhov.trader.trading.simulation.interfaces.Simulator;
import ru.obukhov.trader.web.model.SimulationResult;
import ru.obukhov.trader.web.model.exchange.SimulateRequest;
import ru.obukhov.trader.web.model.exchange.SimulateResponse;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/trader/bot")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class BotController {

    private final Simulator simulator;
    private final ScheduledBotProperties scheduledBotProperties;

    @PostMapping("/simulate")
    public SimulateResponse simulate(@Valid @RequestBody final SimulateRequest request) {

        final Interval interval = DateUtils.getIntervalWithDefaultOffsets(request.getFrom(), request.getTo());
        final boolean saveToFiles = BooleanUtils.isTrue(request.getSaveToFiles());

        final List<SimulationResult> results = simulator.simulate(
                request.getTicker(),
                request.getBalanceConfig(),
                request.getTradingConfigs(),
                interval,
                saveToFiles
        );

        return new SimulateResponse(results);

    }

    @PostMapping("/enable")
    public void enableScheduling() {
        scheduledBotProperties.setEnabled(true);
    }

    @PostMapping("/disable")
    public void disableScheduling() {
        scheduledBotProperties.setEnabled(false);
    }

    @PostMapping("/tickers")
    public void setTickers(@RequestBody final Collection<String> tickers) {
        scheduledBotProperties.setTickers(tickers);
    }

}