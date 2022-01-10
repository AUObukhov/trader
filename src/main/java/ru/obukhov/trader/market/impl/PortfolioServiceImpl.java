package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final TinkoffService tinkoffService;

    @Override
    public List<PortfolioPosition> getPositions(@Nullable final String brokerAccountId) {
        return tinkoffService.getPortfolioPositions(brokerAccountId);
    }

    @Override
    public PortfolioPosition getPosition(@Nullable final String brokerAccountId, final String ticker) {
        final List<PortfolioPosition> allPositions = getPositions(brokerAccountId);
        return allPositions.stream()
                .filter(position -> ticker.equals(position.getTicker()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public BigDecimal getAvailableBalance(@Nullable final String brokerAccountId, final Currency currency) {
        return getCurrencies(brokerAccountId).stream()
                .filter(portfolioCurrency -> portfolioCurrency.getCurrency() == currency)
                .findFirst()
                .map(this::getAvailableBalance)
                .orElseThrow();
    }

    private BigDecimal getAvailableBalance(final CurrencyPosition currency) {
        return currency.getBlocked() == null
                ? currency.getBalance()
                : currency.getBalance().subtract(currency.getBlocked());
    }

    @Override
    public List<CurrencyPosition> getCurrencies(@Nullable final String brokerAccountId) {
        return tinkoffService.getPortfolioCurrencies(brokerAccountId);
    }

}