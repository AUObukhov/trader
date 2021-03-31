package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.tinkoff.invest.openapi.model.rest.Candles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps:
 * <br/>
 * - {@link ru.tinkoff.invest.openapi.model.rest.Candle} to {@link Candle}
 * <br/>
 * - {@link Candles} to list of  {@link Candle}
 */
@Mapper
public abstract class CandleMapper {

    @Mapping(target = "openPrice", source = "o")
    @Mapping(target = "closePrice", source = "c")
    @Mapping(target = "highestPrice", source = "h")
    @Mapping(target = "lowestPrice", source = "l")
    public abstract Candle map(ru.tinkoff.invest.openapi.model.rest.Candle source);

    public List<Candle> map(Candles source) {
        return source.getCandles().stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    protected OffsetDateTime mapOffsetDateTime(OffsetDateTime source) {
        return DateUtils.withDefaultOffset(source);
    }
}
