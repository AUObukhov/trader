package ru.obukhov.investor.model.transform;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.obukhov.investor.model.Candle;
import ru.tinkoff.invest.openapi.models.market.HistoricalCandles;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Mapping(target = "openPrice", source = "openPrice", qualifiedByName = "moneyMapper")
    @Mapping(target = "closePrice", source = "closePrice", qualifiedByName = "moneyMapper")
    @Mapping(target = "highestPrice", source = "highestPrice", qualifiedByName = "moneyMapper")
    @Mapping(target = "lowestPrice", source = "lowestPrice", qualifiedByName = "moneyMapper")
    public abstract Candle map(ru.tinkoff.invest.openapi.models.market.Candle source);

    public List<Candle> map(HistoricalCandles source) {
        return source.candles.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    @AfterMapping
    protected void calculateSaldo(@MappingTarget Candle.CandleBuilder candle,
                                  ru.tinkoff.invest.openapi.models.market.Candle source) {
        candle.saldo(source.closePrice.subtract(source.openPrice).setScale(2, RoundingMode.HALF_UP));
    }

    @Named("moneyMapper")
    protected BigDecimal mapMoney(BigDecimal source) {
        return source.setScale(2, RoundingMode.HALF_UP);
    }
}
