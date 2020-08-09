package ru.obukhov.investor.model.transform;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.obukhov.investor.model.Candle;

@Mapper
public abstract class CandleMapper {
    public abstract Candle mapCandle(ru.tinkoff.invest.openapi.models.market.Candle source);

    @AfterMapping
    protected void calculateSaldo(@MappingTarget Candle candle) {
        candle.setSaldo(candle.getClosePrice().subtract(candle.getOpenPrice()));
    }
}
