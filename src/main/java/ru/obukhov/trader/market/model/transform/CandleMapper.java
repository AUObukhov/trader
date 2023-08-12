package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.trader.market.model.Candle;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

@Mapper(uses = {QuotationMapper.class, DateTimeMapper.class})
public interface CandleMapper {

    Candle map(final HistoricCandle historicCandle);

    @Mapping(target = "open", source = "candle.open")
    @Mapping(target = "close", source = "candle.close")
    @Mapping(target = "high", source = "candle.high")
    @Mapping(target = "low", source = "candle.low")
    HistoricCandle map(final Candle candle, final boolean isComplete);

}