package ru.obukhov.trader.market.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.interfaces.Context;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.FakeBalance;
import ru.obukhov.trader.market.model.FakePortfolio;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.trading.model.BackTestOperation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

/**
 * Prices are loaded from real market, but any operations do not affect the real portfolio - all data is stored in memory.
 */
@Slf4j
public class FakeContext implements Context {

    private final MarketProperties marketProperties;

    @Getter
    @Setter
    private OffsetDateTime currentDateTime;

    private final List<FakePortfolio> portfolios;

    public FakeContext(
            final MarketProperties marketProperties,
            final OffsetDateTime currentDateTime,
            final String accountId,
            final Currency currency,
            final BigDecimal initialBalance
    ) {
        this.marketProperties = marketProperties;
        this.currentDateTime = currentDateTime;
        this.portfolios = new ArrayList<>();

        addInvestment(accountId, currency, initialBalance);
    }

    /**
     * Changes currentDateTime to the nearest work time after it
     *
     * @return new value of currentDateTime
     */
    public OffsetDateTime nextMinute() {
        final OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(currentDateTime, marketProperties.getWorkSchedule());
        currentDateTime = nextWorkMinute;

        return nextWorkMinute;
    }

    // region balance

    /**
     * sets given {@code amount} as balance of given {@code currency} and at given {@code accountId}
     */
    public void setBalance(final String accountId, final Currency currency, final BigDecimal amount) {
        computeIfAbsentBalance(accountId, currency).setCurrentAmount(amount);
    }

    /**
     * @return balance of given {@code currency} and at given {@code accountId}
     */
    public BigDecimal getBalance(final String accountId, final Currency currency) {
        return computeIfAbsentBalance(accountId, currency).getCurrentAmount();
    }

    /**
     * @return balances of all currencies at given {@code accountId}
     */
    public Map<Currency, BigDecimal> getBalances(final String accountId) {
        return computeIfAbsentPortfolio(accountId).getBalances()
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getCurrentAmount()));
    }

    // endregion

    // region investments

    /**
     * Adds given {@code amount} to balance of given {@code currency} and record to history of investments with current dateTime
     */
    public void addInvestment(final String accountId, final Currency currency, final BigDecimal amount) {
        computeIfAbsentBalance(accountId, currency).addInvestment(currentDateTime, amount);
    }

    /**
     * Adds given {@code amount} to balance of given {@code currency} and record to history of investments with given {@code dateTime}
     */
    public void addInvestment(
            final String accountId,
            final OffsetDateTime dateTime,
            final Currency currency,
            final BigDecimal amount
    ) {
        computeIfAbsentBalance(accountId, currency).addInvestment(dateTime, amount);
    }

    /**
     * @return all investments of given {@code currency} and at given {@code accountId} by dateTime in ascending order
     */
    public SortedMap<OffsetDateTime, BigDecimal> getInvestments(final String accountId, final Currency currency) {
        return computeIfAbsentBalance(accountId, currency).getInvestments();
    }

    // endregion

    // region operations

    public void addOperation(final String accountId, final BackTestOperation operation) {
        computeIfAbsentPortfolio(accountId).getOperations().add(operation);
    }

    public Set<BackTestOperation> getOperations(final String accountId) {
        return new HashSet<>(computeIfAbsentPortfolio(accountId).getOperations());
    }

    // endregion

    // region positions

    public void addPosition(final String accountId, final String figi, PortfolioPosition position) {
        computeIfAbsentPortfolio(accountId).getFigiesToPositions().put(figi, position);
    }

    public PortfolioPosition getPosition(final String accountId, final String figi) {
        return computeIfAbsentPortfolio(accountId).getFigiesToPositions().get(figi);
    }

    public List<PortfolioPosition> getPositions(final String accountId) {
        return new ArrayList<>(computeIfAbsentPortfolio(accountId).getFigiesToPositions().values());
    }

    public void removePosition(final String accountId, final String figi) {
        computeIfAbsentPortfolio(accountId).getFigiesToPositions().remove(figi);
    }

    // endregion

    private FakeBalance computeIfAbsentBalance(final String accountId, final Currency currency) {
        return computeIfAbsentPortfolio(accountId).getBalances().computeIfAbsent(currency, currencyKey -> new FakeBalance());
    }

    private FakePortfolio computeIfAbsentPortfolio(final String accountId) {
        final Optional<FakePortfolio> desiredPortfolio = portfolios.stream()
                .filter(portfolio -> accountId.equals(portfolio.getAccountId()))
                .findFirst();
        if (desiredPortfolio.isPresent()) {
            return desiredPortfolio.get();
        } else {
            final FakePortfolio portfolio = new FakePortfolio(accountId);
            portfolios.add(portfolio);
            return portfolio;
        }
    }

}