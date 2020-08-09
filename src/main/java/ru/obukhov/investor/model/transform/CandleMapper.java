package ru.obukhov.investor.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.investor.model.Candle;

@Mapper
public interface CandleMapper {
    Candle mapCandle(ru.tinkoff.invest.openapi.models.market.Candle source);
}
