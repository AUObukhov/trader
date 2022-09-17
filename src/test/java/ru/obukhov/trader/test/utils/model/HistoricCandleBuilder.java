package ru.obukhov.trader.test.utils.model;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.time.OffsetDateTime;

public class HistoricCandleBuilder {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);

    private Double openPrice;
    private Double closePrice;
    private Double highestPrice;
    private Double lowestPrice;
    private OffsetDateTime time;
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
            builder.setTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(time));
        }

        if (isComplete != null) {
            builder.setIsComplete(isComplete);
        }

        return builder.build();
    }

}