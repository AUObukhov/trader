package ru.obukhov.trader.market.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;

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
        this.currentAmount = BigDecimal.ZERO;
        this.investments = new TreeMap<>();
    }

    /**
     * Adds given {@code amount} to balance and record to history of investments with given {@code dateTime}
     */
    public void addInvestment(OffsetDateTime dateTime, BigDecimal amount) {
        Assert.isTrue(amount.signum() > 0, "expected positive investment amount");
        if (investments.containsKey(dateTime)) {
            throw new IllegalArgumentException("investment at " + dateTime + " alreadyExists");
        }

        investments.put(dateTime, amount);
        currentAmount = currentAmount.add(amount);
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments() {
        return new TreeMap<>(investments);
    }
}