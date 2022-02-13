package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.CurrencyPosition;
import ru.obukhov.trader.market.model.PortfolioPosition;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service to get information about customer portfolio
 */
@RequiredArgsConstructor
public class PortfolioService {

    private final TinkoffService tinkoffService;

    public List<PortfolioPosition> getPositions(@Nullable final String brokerAccountId) throws IOException {
        return tinkoffService.getPortfolioPositions(brokerAccountId);
    }

    /**
     * @return position with given {@code ticker} at given {@code brokerAccountId} or null, if such position does not exist.
     * If {@code brokerAccountId} null, works with default broker account
     */
    public PortfolioPosition getPosition(@Nullable final String brokerAccountId, final String ticker) throws IOException {
        final List<PortfolioPosition> allPositions = getPositions(brokerAccountId);
        return allPositions.stream()
                .filter(position -> ticker.equals(position.getTicker()))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return available balance of given {@code currency} at given {@code brokerAccountId}.
     * If {@code brokerAccountId} null, works with default broker account
     * @throws NoSuchElementException if given {@code currency} not found.
     *                                Currencies currently available: {@link Currency.EUR}, {@link Currency.USD}, {@link Currency.RUB}
     */
    public BigDecimal getAvailableBalance(@Nullable final String brokerAccountId, final Currency currency) throws IOException {
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

    /**
     * @return list of currencies balances at given {@code brokerAccountId}.
     * If {@code brokerAccountId} null, works with default broker account
     */
    public List<CurrencyPosition> getCurrencies(@Nullable final String brokerAccountId) throws IOException {
        return tinkoffService.getPortfolioCurrencies(brokerAccountId);
    }

}