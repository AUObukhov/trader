package ru.obukhov.investor.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.investor.model.PortfolioPosition;
import ru.obukhov.investor.service.interfaces.PortfolioService;
import ru.obukhov.investor.web.model.GetPortfolioCurrenciesResponse;
import ru.obukhov.investor.web.model.GetPortfolioPositionsResponse;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/investor/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(@Qualifier("realPortfolioService") PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/positions")
    public GetPortfolioPositionsResponse getPositions() {

        Collection<PortfolioPosition> positions = portfolioService.getPositions();

        return new GetPortfolioPositionsResponse(positions);

    }

    @GetMapping("/currencies")
    public GetPortfolioCurrenciesResponse getCurrencies() {

        List<PortfolioCurrencies.PortfolioCurrency> currencies = portfolioService.getCurrencies();

        return new GetPortfolioCurrenciesResponse(currencies);

    }
}