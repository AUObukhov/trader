package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.Candle;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

@Mapper(uses = {QuotationMapper.class, DateTimeMapper.class})
public interface CandleMapper {

    Candle map(final HistoricCandle historicCandle);

    HistoricCandle map(final Candle candle, final boolean isComplete);

}