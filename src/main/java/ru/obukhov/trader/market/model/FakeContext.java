package ru.obukhov.trader.market.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Class, containing in memory data about simulated portfolio and current market dateTime
 */
public class FakeContext {

    @Getter
    @Setter
    private OffsetDateTime currentDateTime;

    @Getter
    @Setter
    private BigDecimal currentBalance;

    private final Map<String, PortfolioPosition> tickersToPositions;
    private final SortedMap<OffsetDateTime, BigDecimal> investments;
    private final Set<SimulatedOperation> operations;

    public FakeContext(OffsetDateTime currentDateTime, BigDecimal balance) {
        this.currentDateTime = currentDateTime;
        this.currentBalance = balance;

        this.investments = new TreeMap<>();
        this.investments.put(currentDateTime, balance);

        this.operations = new HashSet<>();
        this.tickersToPositions = new HashMap<>();
    }

    public void addInvestment(BigDecimal amount) {
        Assert.isTrue(amount.signum() > 0, "expected positive investment amount");
        if (investments.containsKey(currentDateTime)) {
            throw new IllegalArgumentException("investment at " + currentDateTime + " alreadyExists");
        }

        investments.put(currentDateTime, amount);
        currentBalance = currentBalance.add(amount);
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments() {
        return new TreeMap<>(investments);
    }

    public void addOperation(SimulatedOperation operation) {
        operations.add(operation);
    }

    public Set<SimulatedOperation> getOperations() {
        return new HashSet<>(operations);
    }

    public void addPosition(String ticker, PortfolioPosition position) {
        tickersToPositions.put(ticker, position);
    }

    public void removePosition(String ticker) {
        tickersToPositions.remove(ticker);
    }

    public PortfolioPosition getPosition(String ticker) {
        return tickersToPositions.get(ticker);
    }

    public List<PortfolioPosition> getPositions() {
        return new ArrayList<>(tickersToPositions.values());
    }

}