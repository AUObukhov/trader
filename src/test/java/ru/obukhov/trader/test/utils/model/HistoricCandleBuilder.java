package ru.obukhov.trader.test.utils.model;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

public class HistoricCandleBuilder {

    private Double openPrice;
    private Double closePrice;
    private Double highestPrice;
    private Double lowestPrice;
    private Timestamp time;
    private Boolean isComplete;

    public HistoricCandleBuilder setOpenPrice(final double openPrice) {
        this.openPrice = openPrice;
        return this;
    }

    public HistoricCandleBuilder setClosePrice(final double closePrice) {
        this.closePrice = closePrice;
        return this;
    }

    public HistoricCandleBuilder setHighestPrice(final double highestPrice) {
        this.highestPrice = highestPrice;
        return this;
    }

    public HistoricCandleBuilder setLowestPrice(final double lowestPrice) {
        this.lowestPrice = lowestPrice;
        return this;
    }

    public HistoricCandleBuilder setTime(final Timestamp time) {
        this.time = time;
        return this;
    }

    public HistoricCandleBuilder setIsComplete(final boolean isComplete) {
        this.isComplete = isComplete;
        return this;
    }

    public HistoricCandle build() {
        final HistoricCandle.Builder builder = HistoricCandle.newBuilder();

        if (openPrice != null) {
            builder.setOpen(DecimalUtils.toQuotation(DecimalUtils.setDefaultScale(openPrice)));
        }

        if (closePrice != null) {
            builder.setClose(DecimalUtils.toQuotation(DecimalUtils.setDefaultScale(closePrice)));
        }

        if (highestPrice != null) {
            builder.setHigh(DecimalUtils.toQuotation(DecimalUtils.setDefaultScale(highestPrice)));
        }

        if (lowestPrice != null) {
            builder.setLow(DecimalUtils.toQuotation(DecimalUtils.setDefaultScale(lowestPrice)));
        }

        if (time != null) {
            builder.setTime(time);
        }

        if (isComplete != null) {
            builder.setIsComplete(isComplete);
        }

        return builder.build();
    }

}