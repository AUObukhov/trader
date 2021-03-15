package ru.obukhov.trader.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.bot.interfaces.Simulator;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.BotConfig;
import ru.obukhov.trader.web.model.exchange.SimulateRequest;
import ru.obukhov.trader.web.model.exchange.SimulateResponse;
import ru.obukhov.trader.web.model.pojo.SimulationResult;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/trader/bot")
@RequiredArgsConstructor
public class BotController {

    private final Simulator simulator;
    private final BotConfig botConfig;

    @PostMapping("/simulate")
    public SimulateResponse simulate(@Valid @RequestBody SimulateRequest request) {

        Interval interval = DateUtils.getIntervalWithDefaultOffsets(request.getFrom(), request.getTo());
        boolean saveToFiles = BooleanUtils.isTrue(request.getSaveToFiles());

        Map<String, List<SimulationResult>> results = simulator.simulate(request.getSimulationUnits(), interval, saveToFiles);

        return new SimulateResponse(results);

    }

    @PostMapping("/enable")
    public void enableScheduling() {
        botConfig.setEnabled(true);
    }

    @PostMapping("/disable")
    public void disableScheduling() {
        botConfig.setEnabled(false);
    }

    @PostMapping("/tickers")
    public void setTickers(@RequestBody Collection<String> tickers) {
        botConfig.setTickers(tickers);
    }

}