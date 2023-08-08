package ru.obukhov.trader.test.utils.model;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Candle;

public class CandleBuilder {

    private Double openPrice;
    private Double closePrice;
    private Double highestPrice;
    private Double lowestPrice;
    private Timestamp time;

    public CandleBuilder setOpenPrice(final double openPrice) {
        this.openPrice = openPrice;
        return this;
    }

    public CandleBuilder setClosePrice(final double closePrice) {
        this.closePrice = closePrice;
        return this;
    }

    public CandleBuilder setHighestPrice(final double highestPrice) {
        this.highestPrice = highestPrice;
        return this;
    }

    public CandleBuilder setLowestPrice(final double lowestPrice) {
        this.lowestPrice = lowestPrice;
        return this;
    }

    public CandleBuilder setTime(final Timestamp time) {
        this.time = time;
        return this;
    }

    public Candle build() {
        final Candle candle = new Candle();

        if (openPrice != null) {
            candle.setOpenPrice(DecimalUtils.setDefaultScale(openPrice));
        }

        if (closePrice != null) {
            candle.setClosePrice(DecimalUtils.setDefaultScale(closePrice));
        }

        if (highestPrice != null) {
            candle.setHighestPrice(DecimalUtils.setDefaultScale(highestPrice));
        }

        if (lowestPrice != null) {
            candle.setLowestPrice(DecimalUtils.setDefaultScale(lowestPrice));
        }

        if (time != null) {
            candle.setTime(time);
        }

        return candle;
    }

}