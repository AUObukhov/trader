package ru.obukhov.investor.service.interfaces;

import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.util.List;

public interface PortfolioService {

    List<Portfolio.PortfolioPosition> getPositions(@Nullable String brokerAccountId);

    List<PortfolioCurrencies.PortfolioCurrency> getCurrencies(@Nullable String brokerAccountId);
}