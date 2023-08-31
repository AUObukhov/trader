package ru.obukhov.trader.test.utils.model;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.time.OffsetDateTime;

public class HistoricCandleBuilder {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);

    private Double open;
    private Double close;
    private Double high;
    private Double low;
    private OffsetDateTime time;
    private Boolean isComplete;

    public HistoricCandleBuilder setOpen(final double open) {
        this.open = open;
        return this;
    }

    public HistoricCandleBuilder setClose(final double close) {
        this.close = close;
        return this;
    }

    public HistoricCandleBuilder setHigh(final double high) {
        this.high = high;
        return this;
    }

    public HistoricCandleBuilder setLow(final double low) {
        this.low = low;
        return this;
    }

    public HistoricCandleBuilder setTime(final OffsetDateTime time) {
        this.time = time;
        return this;
    }

    public HistoricCandleBuilder setIsComplete(final boolean isComplete) {
        this.isComplete = isComplete;
        return this;
    }

    public HistoricCandle build() {
        final HistoricCandle.Builder builder = HistoricCandle.newBuilder();

        if (open != null) {
            builder.setOpen(DecimalUtils.toQuotation(DecimalUtils.setDefaultScale(open)));
        }

        if (close != null) {
            builder.setClose(DecimalUtils.toQuotation(DecimalUtils.setDefaultScale(close)));
        }

        if (high != null) {
            builder.setHigh(DecimalUtils.toQuotation(DecimalUtils.setDefaultScale(high)));
        }

        if (low != null) {
            builder.setLow(DecimalUtils.toQuotation(DecimalUtils.setDefaultScale(low)));
        }

        if (time != null) {
            builder.setTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(time));
        }

        if (isComplete != null) {
            builder.setIsComplete(isComplete);
        }

        return builder.build();
    }

}