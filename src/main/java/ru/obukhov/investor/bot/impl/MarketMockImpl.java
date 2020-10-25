package ru.obukhov.investor.bot.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.investor.bot.interfaces.MarketMock;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.util.DateUtils;
import ru.obukhov.investor.util.MathUtils;
import ru.obukhov.investor.web.model.SimulatedOperation;
import ru.obukhov.investor.web.model.SimulatedPosition;
import ru.tinkoff.invest.openapi.models.operations.OperationType;
import ru.tinkoff.invest.openapi.models.orders.Operation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class for keeping fake market and portfolio data, such as current dateTime, balance and positions
 */
@Service
@RequiredArgsConstructor
public class MarketMockImpl implements MarketMock {

    private final TradingProperties tradingProperties;

    private final Set<SimulatedPosition> positions = new HashSet<>();

    @Getter
    private OffsetDateTime currentDateTime;

    @Getter
    @Setter
    private BigDecimal balance;

    @Getter
    private List<SimulatedOperation> operations;

    @Override
    public void init(OffsetDateTime currentDateTime, BigDecimal balance) {
        this.currentDateTime = currentDateTime;
        this.balance = balance;
        this.positions.clear();
        this.operations = new ArrayList<>();
    }

    /**
     * sets current dateTime, but moves it to nearest work time
     */
    @Override
    public void setCurrentDateTime(OffsetDateTime currentDateTime) {
        this.currentDateTime = DateUtils.getNearestWorkTime(currentDateTime,
                tradingProperties.getWorkStartTime(),
                tradingProperties.getWorkDuration());
    }

    /**
     * @return position by {@code ticker} if it is exists, or null other ways
     */
    @Override
    public SimulatedPosition getPosition(String ticker) {
        return this.positions.stream()
                .filter(position -> ticker.equals(position.getTicker()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Adds one minute to current dataTime
     */
    @Override
    public void nextMinute() {

        this.currentDateTime = DateUtils.getNextWorkMinute(
                this.currentDateTime,
                tradingProperties.getWorkStartTime(),
                tradingProperties.getWorkDuration());

    }

    @Override
    public void performOperation(@NotNull String ticker, int quantity, @NotNull Operation operation, BigDecimal price) {
        BigDecimal totalAmount = MathUtils.multiply(price, quantity);
        BigDecimal commission = MathUtils.getFraction(totalAmount, tradingProperties.getCommission());
        BigDecimal balanceChange;
        OperationType operationType;

        if (operation == Operation.Buy) {
            addPosition(createPosition(ticker, quantity, price));

            balanceChange = totalAmount.add(commission).negate();
            operationType = OperationType.Buy;
        } else {
            removePosition(ticker);

            balanceChange = totalAmount.subtract(commission);
            operationType = OperationType.Sell;
        }

        addToBalance(balanceChange);
        addOperation(totalAmount, commission, operationType);
    }

    private SimulatedPosition createPosition(@NotNull String ticker, int quantity, BigDecimal price) {
        return new SimulatedPosition(ticker, price, quantity);
    }

    /**
     * Adds given position to kept positions
     *
     * @throws IllegalArgumentException when position with same ticker already exists
     */
    private void addPosition(SimulatedPosition position) {
        Assert.isNull(getPosition(position.getTicker()),
                "Position with ticker " + position.getTicker() + " already exists");

        this.positions.add(position);
    }

    /**
     * Removes position with given {@code ticker} from  kept positions
     *
     * @throws IllegalArgumentException when position with given {@code ticker} doesn't exists
     */
    private void removePosition(String ticker) {
        boolean result = this.positions.removeIf(position -> ticker.equals(position.getTicker()));
        Assert.isTrue(result, "Position for ticker '" + ticker + "' doesn't exist");
    }

    /**
     * Add given value to balance if balance will be not negative after adding
     *
     * @throws IllegalArgumentException if adding makes balance negative
     */
    private void addToBalance(BigDecimal value) {
        BigDecimal newBalance = this.balance.add(value);

        Assert.isTrue(newBalance.signum() >= 0, "balance can't be negative");

        this.balance = newBalance;
    }

    /**
     * adds operation to {@code operations}
     */
    private void addOperation(BigDecimal totalAmount,
                              BigDecimal commission,
                              OperationType operationType) {

        SimulatedOperation operation
                = new SimulatedOperation(this.currentDateTime, operationType, totalAmount, commission);
        operations.add(operation);
    }

    @Override
    public BigDecimal getFullBalance() {
        BigDecimal fullBalance = this.balance;
        for (SimulatedPosition position : this.positions) {
            fullBalance = fullBalance.add(position.getPrice());
        }
        return fullBalance;
    }

}