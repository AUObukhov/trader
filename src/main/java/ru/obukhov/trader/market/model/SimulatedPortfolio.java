package ru.obukhov.trader.market.model;

import lombok.Data;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class SimulatedPortfolio {

    private OffsetDateTime currentDateTime;
    private BigDecimal balance;
    private final List<SimulatedOperation> operations;
    private final Map<String, PortfolioPosition> tickersToPositions;

    public SimulatedPortfolio(OffsetDateTime currentDateTime, BigDecimal balance) {
        this.currentDateTime = currentDateTime;
        this.balance = balance;
        this.operations = new ArrayList<>();
        this.tickersToPositions = new HashMap<>();
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
