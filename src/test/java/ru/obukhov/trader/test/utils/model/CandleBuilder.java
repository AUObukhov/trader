package ru.obukhov.trader.test.utils.model;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.market.model.Candle;

public class CandleBuilder {

    private Double open;
    private Double close;
    private Double highest;
    private Double lowest;
    private Timestamp time;

    public CandleBuilder setOpen(final double open) {
        this.open = open;
        return this;
    }

    public CandleBuilder setClose(final double close) {
        this.close = close;
        return this;
    }

    public CandleBuilder setHighest(final double highest) {
        this.highest = highest;
        return this;
    }

    public CandleBuilder setLowest(final double lowest) {
        this.lowest = lowest;
        return this;
    }

    public CandleBuilder setTime(final Timestamp time) {
        this.time = time;
        return this;
    }

    public Candle build() {
        final Candle candle = new Candle();

        if (open != null) {
            candle.setOpen(QuotationUtils.newQuotation(open));
        }

        if (close != null) {
            candle.setClose(QuotationUtils.newQuotation(close));
        }

        if (highest != null) {
            candle.setHigh(QuotationUtils.newQuotation(highest));
        }

        if (lowest != null) {
            candle.setLow(QuotationUtils.newQuotation(lowest));
        }

        if (time != null) {
            candle.setTime(time);
        }

        return candle;
    }

}