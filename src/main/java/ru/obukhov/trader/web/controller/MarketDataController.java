package ru.obukhov.trader.web.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

@Slf4j
@RestController
@RequestMapping("/trader/market")
@AllArgsConstructor
@SuppressWarnings("unused")
public class MarketDataController {

    private final ExtMarketDataService extMarketDataService;

    @GetMapping("/status")
    @ApiOperation("Get current trading status")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public SecurityTradingStatus getTradingStatus(@RequestParam @ApiParam(example = "AAPL") final String ticker) {
        return extMarketDataService.getTradingStatus(ticker);
    }

}