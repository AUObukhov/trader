package ru.obukhov.investor.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.investor.bot.interfaces.Simulator;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.web.model.SimulateRequest;
import ru.obukhov.investor.web.model.SimulateResponse;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/investor/bot")
@RequiredArgsConstructor
public class BotController {

    private final Simulator simulator;
    private final TradingProperties tradingProperties;

    @PostMapping("/simulate")
    public SimulateResponse simulate(@Valid @RequestBody SimulateRequest request) {

        return simulator.simulate(request.getTicker(),
                request.getBalance(),
                request.getFrom(),
                request.getTo());

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