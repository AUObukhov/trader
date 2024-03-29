package ru.obukhov.trader.test.utils.model;

import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Candle;

import java.time.OffsetDateTime;

public class CandleBuilder {

    private Double open;
    private Double close;
    private Double high;
    private Double low;
    private OffsetDateTime time;

    public CandleBuilder setOpen(final double open) {
        this.open = open;
        return this;
    }

    public CandleBuilder setClose(final double close) {
        this.close = close;
        return this;
    }

    public CandleBuilder setHigh(final double high) {
        this.high = high;
        return this;
    }

    public CandleBuilder setLow(final double low) {
        this.low = low;
        return this;
    }

    public CandleBuilder setTime(final OffsetDateTime time) {
        this.time = time;
        return this;
    }

    public Candle build() {
        final Candle candle = new Candle();

        if (open != null) {
            candle.setOpen(DecimalUtils.setDefaultScale(open));
        }

        if (close != null) {
            candle.setClose(DecimalUtils.setDefaultScale(close));
        }

        if (high != null) {
            candle.setHigh(DecimalUtils.setDefaultScale(high));
        }

        if (low != null) {
            candle.setLow(DecimalUtils.setDefaultScale(low));
        }

        if (time != null) {
            candle.setTime(time);
        }

        return candle;
    }

}