package ru.obukhov.trader.market.model;

import lombok.Getter;
import lombok.Setter;
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
 * Class, containing in memory data about back tested portfolio and current market dateTime
 */
public class FakeContext {

    @Getter
    @Setter
    private OffsetDateTime currentDateTime;

    private final List<FakePortfolio> portfolios;

    public FakeContext(final OffsetDateTime currentDateTime) {
        this.currentDateTime = currentDateTime;
        this.portfolios = new ArrayList<>();
    }

    public FakeContext(
            final OffsetDateTime currentDateTime,
            final String accountId,
            final Currency currency,
            final BigDecimal initialBalance
    ) {
        this(currentDateTime);

        addInvestment(accountId, currency, initialBalance);
    }

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

    public BigDecimal getBalance(final String accountId, final Currency currency) {
        return computeIfAbsentBalance(accountId, currency).getCurrentAmount();
    }

    public Map<Currency, BigDecimal> getBalances(final String accountId) {
        return computeIfAbsentPortfolio(accountId).getBalances()
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getCurrentAmount()));
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments(final String accountId, final Currency currency) {
        return computeIfAbsentBalance(accountId, currency).getInvestments();
    }

    public void setCurrentBalance(final String accountId, final Currency currency, final BigDecimal amount) {
        computeIfAbsentBalance(accountId, currency).setCurrentAmount(amount);
    }

    public void addOperation(final String accountId, final BackTestOperation operation) {
        computeIfAbsentPortfolio(accountId).getOperations().add(operation);
    }

    public Set<BackTestOperation> getOperations(final String accountId) {
        return new HashSet<>(computeIfAbsentPortfolio(accountId).getOperations());
    }

    public void addPosition(final String accountId, final String ticker, PortfolioPosition position) {
        computeIfAbsentPortfolio(accountId).getTickersToPositions().put(ticker, position);
    }

    public void removePosition(final String accountId, final String ticker) {
        computeIfAbsentPortfolio(accountId).getTickersToPositions().remove(ticker);
    }

    public PortfolioPosition getPosition(final String accountId, final String ticker) {
        return computeIfAbsentPortfolio(accountId).getTickersToPositions().get(ticker);
    }

    public List<PortfolioPosition> getPositions(final String accountId) {
        return new ArrayList<>(computeIfAbsentPortfolio(accountId).getTickersToPositions().values());
    }

    private FakeBalance computeIfAbsentBalance(final String accountId, final Currency currency) {
        return computeIfAbsentPortfolio(accountId).getBalances().computeIfAbsent(currency, currencyKey -> new FakeBalance());
    }

    private FakePortfolio computeIfAbsentPortfolio(final String accountId) {
        if (accountId == null) {
            if (portfolios.isEmpty()) {
                portfolios.add(new FakePortfolio(null));
            }
            return portfolios.get(0);
        } else {
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

}