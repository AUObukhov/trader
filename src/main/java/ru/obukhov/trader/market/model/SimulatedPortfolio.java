package ru.obukhov.trader.market.model;

import lombok.Data;
import org.springframework.util.Assert;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Data
public class SimulatedPortfolio {

    private OffsetDateTime currentDateTime;
    private BigDecimal currentBalance;

    private final Map<String, PortfolioPosition> tickersToPositions;
    private final SortedMap<OffsetDateTime, BigDecimal> investments;
    private final List<SimulatedOperation> operations;

    public SimulatedPortfolio(OffsetDateTime currentDateTime, BigDecimal balance) {
        this.currentDateTime = currentDateTime;
        this.currentBalance = balance;

        this.investments = new TreeMap<>();
        this.investments.put(currentDateTime, balance);

        this.operations = new ArrayList<>();
        this.tickersToPositions = new HashMap<>();
    }

    public void addInvestment(BigDecimal amount) {
        Assert.isTrue(amount.signum() >= 0, "investment amount can't be negative");

        investments.put(currentDateTime, amount);
        currentBalance = currentBalance.add(amount);
    }

    public void addOperation(SimulatedOperation operation) {
        operations.add(operation);
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

}