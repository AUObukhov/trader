package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service to get information about customer portfolio
 */
@RequiredArgsConstructor
public class PortfolioService {

    private final TinkoffService tinkoffService;

    public List<PortfolioPosition> getPositions(@Nullable final String brokerAccountId) {
        return tinkoffService.getPortfolioPositions(brokerAccountId);
    }

    /**
     * @return position with given {@code ticker} at given {@code brokerAccountId} or null, if such position does not exist
     */
    public PortfolioPosition getPosition(@Nullable final String brokerAccountId, final String ticker) {
        final List<PortfolioPosition> allPositions = getPositions(brokerAccountId);
        return allPositions.stream()
                .filter(position -> ticker.equals(position.getTicker()))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return available balance of given {@code currency} at given {@code brokerAccountId}
     * @throws NoSuchElementException if given {@code currency} not found.
     *                                Currencies currently available: {@link Currency.EUR}, {@link Currency.USD}, {@link Currency.RUB}
     */
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

    /**
     * @return list of currencies balances at given {@code brokerAccountId}
     */
    public List<CurrencyPosition> getCurrencies(@Nullable final String brokerAccountId) {
        return tinkoffService.getPortfolioCurrencies(brokerAccountId);
    }

}