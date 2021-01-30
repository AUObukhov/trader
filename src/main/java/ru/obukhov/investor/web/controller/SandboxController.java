package ru.obukhov.investor.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.investor.service.interfaces.SandboxService;
import ru.obukhov.investor.web.model.ClearAllRequest;
import ru.obukhov.investor.web.model.SetCurrencyBalanceRequest;
import ru.obukhov.investor.web.model.SetPositionBalanceRequest;

@Slf4j
@RestController
@RequestMapping("/investor/sandbox")
@RequiredArgsConstructor
@ConditionalOnProperty(value = "trading.sandbox", havingValue = "true")
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