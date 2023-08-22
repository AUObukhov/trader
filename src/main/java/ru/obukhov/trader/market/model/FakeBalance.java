package ru.obukhov.trader.market.model;

import com.google.protobuf.Timestamp;
import lombok.Getter;
import lombok.Setter;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Class keeping current balance and history of investments (not direct changes)
 */
public class FakeBalance {

    @Getter
    @Setter
    private Quotation currentAmount;

    private final SortedMap<Timestamp, Quotation> investments;

    public FakeBalance() {
        this.currentAmount = QuotationUtils.newQuotation(0, 0);
        this.investments = new TreeMap<>(TimestampUtils::compare);
    }

    /**
     * Adds given {@code amount} to balance and record to history of investments with given {@code timestamp}
     */
    public void addInvestment(final Timestamp timestamp, final Quotation amount) {
        final Quotation newAmount = investments.containsKey(timestamp) ? QuotationUtils.add(investments.get(timestamp), amount) : amount;
        investments.put(timestamp, newAmount);
        currentAmount = QuotationUtils.add(currentAmount, amount);
    }

    public SortedMap<Timestamp, Quotation> getInvestments() {
        return new TreeMap<>(investments);
    }
}