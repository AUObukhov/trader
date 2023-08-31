package ru.obukhov.trader.market.model;

import lombok.Getter;
import lombok.Setter;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.time.OffsetDateTime;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Class keeping current balance and history of investments (not direct changes)
 */
public class FakeBalance {

    @Getter
    @Setter
    private Quotation currentAmount;

    private final SortedMap<OffsetDateTime, Quotation> investments;

    public FakeBalance() {
        this.currentAmount = QuotationUtils.newQuotation(0, 0);
        this.investments = new TreeMap<>();
    }

    /**
     * Adds given {@code amount} to balance and record to history of investments with given {@code dateTime}
     */
    public void addInvestment(final OffsetDateTime dateTime, final Quotation amount) {
        final Quotation newAmount = investments.containsKey(dateTime) ? QuotationUtils.add(investments.get(dateTime), amount) : amount;
        investments.put(dateTime, newAmount);
        currentAmount = QuotationUtils.add(currentAmount, amount);
    }

    public SortedMap<OffsetDateTime, Quotation> getInvestments() {
        return new TreeMap<>(investments);
    }
}