package ru.obukhov.trader.market.model;

import lombok.Getter;
import lombok.Setter;
import ru.obukhov.trader.common.util.DecimalUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Class keeping current balance and history of investments (not direct changes)
 */
public class FakeBalance {

    @Getter
    @Setter
    private BigDecimal currentAmount;

    private final SortedMap<OffsetDateTime, BigDecimal> investments;

    public FakeBalance() {
        this.currentAmount = DecimalUtils.ZERO;
        this.investments = new TreeMap<>();
    }

    /**
     * Adds given {@code amount} to balance and record to history of investments with given {@code dateTime}
     */
    public void addInvestment(final OffsetDateTime dateTime, final BigDecimal amount) {
        final BigDecimal newAmount = investments.containsKey(dateTime) ? investments.get(dateTime).add(amount) : amount;
        investments.put(dateTime, newAmount);
        currentAmount = currentAmount.add(amount);
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments() {
        return new TreeMap<>(investments);
    }
}