package ru.obukhov.investor.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.investor.service.interfaces.PortfolioService;
import ru.obukhov.investor.web.model.GetPortfolioCurrenciesResponse;
import ru.obukhov.investor.web.model.GetPortfolioPositionsResponse;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/investor/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/positions")
    public GetPortfolioPositionsResponse getPositions() {

        List<Portfolio.PortfolioPosition> positions = portfolioService.getPositions();

        return new GetPortfolioPositionsResponse(positions);

    }

    @GetMapping("/currencies")
    public GetPortfolioCurrenciesResponse getCurrencies() {

        List<PortfolioCurrencies.PortfolioCurrency> currencies = portfolioService.getCurrencies();

        return new GetPortfolioCurrenciesResponse(currencies);

    }
}