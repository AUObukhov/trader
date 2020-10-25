package ru.obukhov.investor.service.impl;

import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.service.TinkoffContextsAware;
import ru.obukhov.investor.service.interfaces.ConnectionService;
import ru.obukhov.investor.service.interfaces.PortfolioService;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PortfolioServiceImpl extends TinkoffContextsAware implements PortfolioService {

    public PortfolioServiceImpl(ConnectionService connectionService) {
        super(connectionService);
    }

    @Override
    public List<Portfolio.PortfolioPosition> getPositions() {
        return getPositions(null);
    }

    @Override
    public List<Portfolio.PortfolioPosition> getPositions(@Nullable String brokerAccountId) {
        return getPortfolioContext().getPortfolio(brokerAccountId).join().positions;
    }

    @Override
    public Portfolio.PortfolioPosition getPosition(String ticker) {
        List<Portfolio.PortfolioPosition> allPositions = getPositions();
        return allPositions.stream()
                .filter(position -> ticker.equals(position.ticker))
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
        return getCurrencies(null);
    }

    @Override
    public List<PortfolioCurrencies.PortfolioCurrency> getCurrencies(@Nullable String brokerAccountId) {
        return getPortfolioContext().getPortfolioCurrencies(brokerAccountId).join().currencies;
    }

}