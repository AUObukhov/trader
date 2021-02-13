package ru.obukhov.investor.service.impl;

import lombok.RequiredArgsConstructor;
import ru.obukhov.investor.bot.interfaces.TinkoffService;
import ru.obukhov.investor.model.PortfolioPosition;
import ru.obukhov.investor.service.interfaces.PortfolioService;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final TinkoffService tinkoffService;

    @Override
    public Collection<PortfolioPosition> getPositions() {
        return tinkoffService.getPortfolioPositions();
    }

    @Override
    public PortfolioPosition getPosition(String ticker) {
        Collection<PortfolioPosition> allPositions = getPositions();
        return allPositions.stream()
                .filter(position -> ticker.equals(position.getTicker()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public BigDecimal getAvailableBalance(Currency currency) {
        return getCurrencies().stream()
                .filter(portfolioCurrency -> portfolioCurrency.currency == currency)
                .findFirst()
                .map(this::getAvailableBalance)
                .orElseThrow();
    }

    private BigDecimal getAvailableBalance(PortfolioCurrencies.PortfolioCurrency currency) {
        return currency.blocked == null
                ? currency.balance
                : currency.balance.subtract(currency.blocked);
    }

    @Override
    public List<PortfolioCurrencies.PortfolioCurrency> getCurrencies() {
        return tinkoffService.getPortfolioCurrencies();
    }

}