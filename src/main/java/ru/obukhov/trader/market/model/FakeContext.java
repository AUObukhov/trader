package ru.obukhov.trader.market.model;

import lombok.Getter;
import lombok.Setter;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;
import ru.tinkoff.invest.openapi.model.rest.Currency;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
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

    private final EnumMap<Currency, FakeBalance> balances;
    private final Map<String, PortfolioPosition> tickersToPositions;
    private final Set<SimulatedOperation> operations;

    public FakeContext(final OffsetDateTime currentDateTime) {
        this.currentDateTime = currentDateTime;

        this.balances = new EnumMap<>(Currency.class);

        this.operations = new HashSet<>();
        this.tickersToPositions = new HashMap<>();
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
        return Stream.of(Currency.values())
                .collect(Collectors.toMap(currency -> currency, this::getBalance));
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments(final Currency currency) {
        return computeIfAbsentFakeBalance(currency).getInvestments();
    }

    public void setCurrentBalance(final Currency currency, final BigDecimal amount) {
        computeIfAbsentFakeBalance(currency).setCurrentAmount(amount);
    }

    public void addOperation(final SimulatedOperation operation) {
        operations.add(operation);
    }

    public Set<SimulatedOperation> getOperations() {
        return new HashSet<>(operations);
    }

    public void addPosition(final String ticker, PortfolioPosition position) {
        tickersToPositions.put(ticker, position);
    }

    public void removePosition(final String ticker) {
        tickersToPositions.remove(ticker);
    }

    public PortfolioPosition getPosition(final String ticker) {
        return tickersToPositions.get(ticker);
    }

    public List<PortfolioPosition> getPositions() {
        return new ArrayList<>(tickersToPositions.values());
    }

    private FakeBalance computeIfAbsentFakeBalance(final Currency currency) {
        return balances.computeIfAbsent(currency, currencyKey -> new FakeBalance());
    }

}