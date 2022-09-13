package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.trader.market.model.Etf;

/**
 * Maps {@link ru.tinkoff.piapi.contract.v1.Etf} to {@link Etf}
 */
@Mapper(uses = {SectorMapper.class, QuotationMapper.class, DateTimeMapper.class, MoneyMapper.class, CurrencyMapper.class})
public interface EtfMapper {

    @Mapping(target = "lotSize", source = "lot")
    @Mapping(target = "country", source = "countryOfRiskName")
    @Mapping(target = "buyAvailable", source = "buyAvailableFlag")
    @Mapping(target = "sellAvailable", source = "sellAvailableFlag")
    @Mapping(target = "apiTradeAvailable", source = "apiTradeAvailableFlag")
    Etf map(final ru.tinkoff.piapi.contract.v1.Etf source);
}