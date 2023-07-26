package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.trader.market.model.Share;

/**
 * Maps {@link ru.tinkoff.piapi.contract.v1.Share} to {@link Share}
 */
@Mapper(uses = {SectorMapper.class, QuotationMapper.class, DateTimeMapper.class, MoneyMapper.class})
public interface ShareMapper {

    @Mapping(target = "lotSize", source = "lot")
    @Mapping(target = "country", source = "countryOfRiskName")
    @Mapping(target = "buyAvailable", source = "buyAvailableFlag")
    @Mapping(target = "sellAvailable", source = "sellAvailableFlag")
    @Mapping(target = "apiTradeAvailable", source = "apiTradeAvailableFlag")
    Share map(final ru.tinkoff.piapi.contract.v1.Share source);

}