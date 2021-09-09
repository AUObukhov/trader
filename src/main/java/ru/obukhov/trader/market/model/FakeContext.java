package ru.obukhov.trader.market.model;

import lombok.Getter;
import lombok.Setter;
import ru.obukhov.trader.web.model.SimulatedOperation;
import ru.tinkoff.invest.openapi.model.rest.Currency;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class, containing in memory data about simulated portfolio and current market dateTime
 */
public class FakeContext {

    @Getter
    @Setter
    private OffsetDateTime currentDateTime;

    private final FakePortfolio portfolio;

    public FakeContext(final OffsetDateTime currentDateTime) {
        this.currentDateTime = currentDateTime;

        this.portfolio = new FakePortfolio();
    }

    /**
     * Adds given {@code amount} to balance of given {@code currency} and record to history of investments with current
     * dateTime
     */
    public void addInvestment(final Currency currency, final BigDecimal amount) {
        computeIfAbsentFakeBalance(currency).addInvestment(currentDateTime, amount);
    }

    public BigDecimal getBalance(final Currency currency) {
        return computeIfAbsentFakeBalance(currency).getCurrentAmount();
    }

    public Map<Currency, BigDecimal> getBalances() {
        return Stream.of(Currency.values()).collect(Collectors.toMap(currency -> currency, this::getBalance));
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments(final Currency currency) {
        return computeIfAbsentFakeBalance(currency).getInvestments();
    }

    public void setCurrentBalance(final Currency currency, final BigDecimal amount) {
        computeIfAbsentFakeBalance(currency).setCurrentAmount(amount);
    }

    public void addOperation(final SimulatedOperation operation) {
        portfolio.getOperations().add(operation);
    }

    public Set<SimulatedOperation> getOperations() {
        return new HashSet<>(portfolio.getOperations());
    }

    public void addPosition(final String ticker, PortfolioPosition position) {
        portfolio.getTickersToPositions().put(ticker, position);
    }

    public void removePosition(final String ticker) {
        portfolio.getTickersToPositions().remove(ticker);
    }

    public PortfolioPosition getPosition(final String ticker) {
        return portfolio.getTickersToPositions().get(ticker);
    }

    public List<PortfolioPosition> getPositions() {
        return new ArrayList<>(portfolio.getTickersToPositions().values());
    }

    private FakeBalance computeIfAbsentFakeBalance(final Currency currency) {
        return portfolio.getBalances().computeIfAbsent(currency, currencyKey -> new FakeBalance());
    }

}