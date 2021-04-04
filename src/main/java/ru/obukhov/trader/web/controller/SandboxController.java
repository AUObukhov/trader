package ru.obukhov.trader.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.market.interfaces.SandboxService;
import ru.obukhov.trader.web.model.exchange.ClearAllRequest;
import ru.obukhov.trader.web.model.exchange.SetCurrencyBalanceRequest;
import ru.obukhov.trader.web.model.exchange.SetPositionBalanceRequest;

@Slf4j
@RestController
@RequestMapping("/trader/sandbox")
@RequiredArgsConstructor
@ConditionalOnProperty(value = "trading.sandbox", havingValue = "true")
@SuppressWarnings("unused")
public class SandboxController {

    private final SandboxService sandboxService;

    @PostMapping("/currency-balance")
    public void setCurrencyBalance(@RequestBody SetCurrencyBalanceRequest request) {

        sandboxService.setCurrencyBalance(request.getCurrency(), request.getBalance(), request.getBrokerAccountId());

    }

    @PostMapping("/position-balance")
    public void setPositionBalance(@RequestBody SetPositionBalanceRequest request) {

        sandboxService.setPositionBalance(request.getTicker(), request.getBalance(), request.getBrokerAccountId());

    }

    @PostMapping("/clear")
    public void clearAll(@RequestBody ClearAllRequest request) {

        sandboxService.clearAll(request.getBrokerAccountId());

    }
}