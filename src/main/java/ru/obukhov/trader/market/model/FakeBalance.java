package ru.obukhov.trader.market.model;

import com.google.protobuf.Timestamp;
import lombok.Getter;
import lombok.Setter;
import ru.obukhov.trader.common.util.TimestampUtils;

import java.math.BigDecimal;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Class keeping current balance and history of investments (not direct changes)
 */
public class FakeBalance {

    @Getter
    @Setter
    private BigDecimal currentAmount;

    private final SortedMap<Timestamp, BigDecimal> investments;

    public FakeBalance() {
        this.currentAmount = BigDecimal.ZERO;
        this.investments = new TreeMap<>(TimestampUtils::compare);
    }

    /**
     * Adds given {@code amount} to balance and record to history of investments with given {@code timestamp}
     */
    public void addInvestment(final Timestamp timestamp, final BigDecimal amount) {
        final BigDecimal newAmount = investments.containsKey(timestamp) ? investments.get(timestamp).add(amount) : amount;
        investments.put(timestamp, newAmount);
        currentAmount = currentAmount.add(amount);
    }

    public SortedMap<Timestamp, BigDecimal> getInvestments() {
        return new TreeMap<>(investments);
    }
}