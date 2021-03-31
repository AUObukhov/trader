package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;

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
                .filter(portfolioCurrency -> portfolioCurrency.getCurrency() == currency)
                .findFirst()
                .map(this::getAvailableBalance)
                .orElseThrow();
    }

    private BigDecimal getAvailableBalance(CurrencyPosition currency) {
        return currency.getBlocked() == null
                ? currency.getBalance()
                : currency.getBalance().subtract(currency.getBlocked());
    }

    @Override
    public List<CurrencyPosition> getCurrencies() {
        return tinkoffService.getPortfolioCurrencies();
    }

}