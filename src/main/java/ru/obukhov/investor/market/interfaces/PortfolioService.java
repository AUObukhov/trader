package ru.obukhov.investor.market.interfaces;

import ru.obukhov.investor.market.model.PortfolioPosition;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface PortfolioService {

    Collection<PortfolioPosition> getPositions();

    PortfolioPosition getPosition(String ticker);

    BigDecimal getAvailableBalance(Currency currency);

    List<PortfolioCurrencies.PortfolioCurrency> getCurrencies();

}