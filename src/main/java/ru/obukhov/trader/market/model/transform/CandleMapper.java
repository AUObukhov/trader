package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.trader.market.model.Candle;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

@Mapper(uses = {QuotationMapper.class, DateTimeMapper.class})
public interface CandleMapper {

    @Mapping(target = "openPrice", source = "open")
    @Mapping(target = "closePrice", source = "close")
    @Mapping(target = "highestPrice", source = "high")
    @Mapping(target = "lowestPrice", source = "low")
    Candle map(final HistoricCandle historicCandle);

    @Mapping(target = "open", source = "candle.openPrice")
    @Mapping(target = "close", source = "candle.closePrice")
    @Mapping(target = "high", source = "candle.highestPrice")
    @Mapping(target = "low", source = "candle.lowestPrice")
    HistoricCandle map(final Candle candle, final boolean isComplete);

}