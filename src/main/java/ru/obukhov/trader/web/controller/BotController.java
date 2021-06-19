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
import ru.obukhov.trader.config.ScheduledBotConfig;
import ru.obukhov.trader.trading.simulation.interfaces.Simulator;
import ru.obukhov.trader.web.model.exchange.SimulateRequest;
import ru.obukhov.trader.web.model.exchange.SimulateResponse;
import ru.obukhov.trader.web.model.pojo.SimulationResult;

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
    private final ScheduledBotConfig scheduledBotConfig;

    @PostMapping("/simulate")
    public SimulateResponse simulate(@Valid @RequestBody final SimulateRequest request) {

        final Interval interval = DateUtils.getIntervalWithDefaultOffsets(request.getFrom(), request.getTo());
        final boolean saveToFiles = BooleanUtils.isTrue(request.getSaveToFiles());

        final List<SimulationResult> results = simulator.simulate(
                request.getTicker(),
                request.getInitialBalance(),
                request.getBalanceIncrement(),
                request.getBalanceIncrementCron(),
                request.getStrategiesConfigs(),
                interval,
                saveToFiles
        );

        return new SimulateResponse(results);

    }

    @PostMapping("/enable")
    public void enableScheduling() {
        scheduledBotConfig.setEnabled(true);
    }

    @PostMapping("/disable")
    public void disableScheduling() {
        scheduledBotConfig.setEnabled(false);
    }

    @PostMapping("/tickers")
    public void setTickers(@RequestBody final Collection<String> tickers) {
        scheduledBotConfig.setTickers(tickers);
    }

}