package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.trader.market.model.Bond;

/**
 * Maps {@link ru.tinkoff.piapi.contract.v1.Bond} to {@link Bond}
 */
@Mapper(uses = {SectorMapper.class, QuotationMapper.class, DateTimeMapper.class, MoneyMapper.class, CurrencyMapper.class, ExchangeMapper.class})
public interface BondMapper {

    @Mapping(target = "lotSize", source = "lot")
    @Mapping(target = "country", source = "countryOfRiskName")
    @Mapping(target = "buyAvailable", source = "buyAvailableFlag")
    @Mapping(target = "sellAvailable", source = "sellAvailableFlag")
    @Mapping(target = "apiTradeAvailable", source = "apiTradeAvailableFlag")
    Bond map(final ru.tinkoff.piapi.contract.v1.Bond source);
}