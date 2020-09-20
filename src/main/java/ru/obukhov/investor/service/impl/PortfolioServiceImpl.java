package ru.obukhov.investor.service.impl;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.service.interfaces.PortfolioService;
import ru.tinkoff.invest.openapi.PortfolioContext;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.util.List;

@Service
@AllArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioContext portfolioContext;

    @Override
    public List<Portfolio.PortfolioPosition> getPositions(@Nullable String brokerAccountId) {
        return portfolioContext.getPortfolio(brokerAccountId).join().positions;
    }

    @Override
    public List<PortfolioCurrencies.PortfolioCurrency> getCurrencies(@Nullable String brokerAccountId) {
        return portfolioContext.getPortfolioCurrencies(brokerAccountId).join().currencies;
    }

}