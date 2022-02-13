package ru.obukhov.trader.web.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.market.impl.PortfolioService;
import ru.obukhov.trader.market.model.CurrencyPosition;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.web.model.exchange.GetPortfolioCurrenciesResponse;
import ru.obukhov.trader.web.model.exchange.GetPortfolioPositionsResponse;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/trader/portfolio")
@SuppressWarnings("unused")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(final PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/positions")
    @ApiOperation("Get positions of portfolio at broker account")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public GetPortfolioPositionsResponse getPositions(
            @RequestParam(required = false)
            @ApiParam(name = "brokerAccountId. When null then default account used", example = "2008941383") final String brokerAccountId
    ) throws IOException {
        final List<PortfolioPosition> positions = portfolioService.getPositions(brokerAccountId);

        return new GetPortfolioPositionsResponse(positions);
    }

    @GetMapping("/currencies")
    @ApiOperation("Get currencies of portfolio at default broker account")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public GetPortfolioCurrenciesResponse getCurrencies(
            @RequestParam(required = false)
            @ApiParam(name = "brokerAccountId. When null then default account used", example = "2008941383") final String brokerAccountId
    ) throws IOException {
        final List<CurrencyPosition> currencies = portfolioService.getCurrencies(brokerAccountId);

        return new GetPortfolioCurrenciesResponse(currencies);
    }

}