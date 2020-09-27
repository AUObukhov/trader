package ru.obukhov.investor.service.interfaces;

import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.math.BigDecimal;
import java.util.List;

public interface PortfolioService {

    List<Portfolio.PortfolioPosition> getPositions();

    List<Portfolio.PortfolioPosition> getPositions(@Nullable String brokerAccountId);

    Portfolio.PortfolioPosition getPosition(String ticker);

    BigDecimal getAvailableBalance(Currency currency);

    List<PortfolioCurrencies.PortfolioCurrency> getCurrencies();

    List<PortfolioCurrencies.PortfolioCurrency> getCurrencies(@Nullable String brokerAccountId);
}