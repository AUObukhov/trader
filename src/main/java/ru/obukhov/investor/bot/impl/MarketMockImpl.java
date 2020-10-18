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
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.MoneyAmount;
import ru.tinkoff.invest.openapi.models.operations.OperationStatus;
import ru.tinkoff.invest.openapi.models.operations.OperationType;
import ru.tinkoff.invest.openapi.models.orders.Operation;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Class for keeping fake market and portfolio data, such as current dateTime, balance and positions
 */
@Service
@RequiredArgsConstructor
public class MarketMockImpl implements MarketMock {

    private final TradingProperties tradingProperties;

    private final Set<Portfolio.PortfolioPosition> positions = new HashSet<>();

    @Getter
    private OffsetDateTime currentDateTime;

    @Getter
    @Setter
    private BigDecimal balance;

    @Getter
    private List<ru.tinkoff.invest.openapi.models.operations.Operation> operations;

    @Override
    public void init(OffsetDateTime currentDateTime, BigDecimal balance) {
        this.currentDateTime = currentDateTime;
        this.balance = balance;
        this.positions.clear();
        this.operations = new ArrayList<>();
    }

    /**
     * sets current dateTime, but moves it to nearest work time
     *
     * @param currentDateTime
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
    public Portfolio.PortfolioPosition getPosition(String ticker) {
        return this.positions.stream()
                .filter(position -> ticker.equals(position.ticker))
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
    public void performOperation(@NotNull String ticker, int lots, @NotNull Operation operation, BigDecimal price) {
        if (operation == Operation.Buy) {
            Portfolio.PortfolioPosition position = createPosition(ticker, lots, price);
            addPosition(position);

            BigDecimal balanceChange = MathUtils.addFraction(price, tradingProperties.getCommission()).negate();
            addToBalance(balanceChange);

            addOperation(lots, price, OperationType.Buy);
        } else {
            removePosition(ticker);

            BigDecimal balanceChange = MathUtils.subtractFraction(price, tradingProperties.getCommission());
            addToBalance(balanceChange);

            addOperation(lots, price, OperationType.Sell);
        }
    }

    private Portfolio.PortfolioPosition createPosition(@NotNull String ticker, int lots, BigDecimal price) {
        MoneyAmount averagePositionPrice = new MoneyAmount(Currency.RUB, price);
        return new Portfolio.PortfolioPosition(
                null,
                ticker,
                null,
                null,
                price,
                null,
                null,
                lots,
                averagePositionPrice,
                null,
                null);
    }

    /**
     * Adds given position to kept positions
     *
     * @throws IllegalArgumentException when position with same ticker already exists
     */
    private void addPosition(Portfolio.PortfolioPosition position) {
        Assert.isNull(getPosition(position.ticker),
                "Position with ticker " + position.ticker + " already exists");

        this.positions.add(position);
    }

    /**
     * Removes position with given {@code ticker} from  kept positions
     *
     * @throws IllegalArgumentException when position with given {@code ticker} doesn't exists
     */
    private void removePosition(String ticker) {
        boolean result = this.positions.removeIf(position -> ticker.equals(position.ticker));
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
    private void addOperation(int lots, BigDecimal price, OperationType operationType) {
        ru.tinkoff.invest.openapi.models.operations.Operation historyOperation
                = new ru.tinkoff.invest.openapi.models.operations.Operation(
                "no id",
                OperationStatus.Done,
                createTrades(lots, price),
                createCommission(price),
                Currency.RUB,
                MathUtils.multiply(price, lots),
                price,
                lots,
                null,
                null,
                false,
                currentDateTime,
                operationType
        );

        operations.add(historyOperation);
    }

    private List<ru.tinkoff.invest.openapi.models.operations.Operation.Trade> createTrades(int lots, BigDecimal price) {
        ru.tinkoff.invest.openapi.models.operations.Operation.Trade trade =
                new ru.tinkoff.invest.openapi.models.operations.Operation.Trade(
                        "no id",
                        currentDateTime,
                        price,
                        lots
                );
        return newArrayList(trade);
    }

    private MoneyAmount createCommission(BigDecimal price) {
        return new MoneyAmount(Currency.RUB, MathUtils.getFraction(price, tradingProperties.getCommission()));
    }
}