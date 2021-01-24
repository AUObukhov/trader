package ru.obukhov.investor.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.util.DateUtils;
import ru.tinkoff.invest.openapi.models.market.HistoricalCandles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps:
 * <br/>
 * - {@link ru.tinkoff.invest.openapi.models.market.Candle} to {@link ru.obukhov.investor.model.Candle}
 * <br/>
 * - {@link ru.tinkoff.invest.openapi.models.market.HistoricalCandles} to list of  {@link ru.obukhov.investor.model.Candle}
 */
@Mapper
public abstract class CandleMapper {

    public abstract Candle map(ru.tinkoff.invest.openapi.models.market.Candle source);

    public List<Candle> map(HistoricalCandles source) {
        return source.candles.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    protected OffsetDateTime mapOffsetDateTime(OffsetDateTime source) {
        return DateUtils.withDefaultOffset(source);
    }
}
