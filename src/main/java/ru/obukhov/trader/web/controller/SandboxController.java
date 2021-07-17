package ru.obukhov.trader.web.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
    @ApiOperation("Set sandbox currency balance")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public void setCurrencyBalance(@RequestBody final SetCurrencyBalanceRequest request) {
        sandboxService.setCurrencyBalance(request.getCurrency(), request.getBalance(), request.getBrokerAccountId());
    }

    @PostMapping("/position-balance")
    @ApiOperation("Set sandbox position balance")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public void setPositionBalance(@RequestBody final SetPositionBalanceRequest request) {
        sandboxService.setPositionBalance(request.getTicker(), request.getBalance(), request.getBrokerAccountId());
    }

    @PostMapping("/clear")
    @ApiOperation(value = "Clear sandbox state")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public void clearAll(@RequestBody final ClearAllRequest request) {
        sandboxService.clearAll(request.getBrokerAccountId());
    }
}