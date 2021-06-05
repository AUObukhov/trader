package ru.obukhov.trader.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.web.model.exchange.GetPortfolioCurrenciesResponse;
import ru.obukhov.trader.web.model.exchange.GetPortfolioPositionsResponse;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;

import java.util.Collection;
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
    public GetPortfolioPositionsResponse getPositions() {
        final Collection<PortfolioPosition> positions = portfolioService.getPositions();

        return new GetPortfolioPositionsResponse(positions);
    }

    @GetMapping("/currencies")
    public GetPortfolioCurrenciesResponse getCurrencies() {
        final List<CurrencyPosition> currencies = portfolioService.getCurrencies();

        return new GetPortfolioCurrenciesResponse(currencies);
    }
}