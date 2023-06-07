package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.trader.market.model.CurrencyInstrument;
import ru.tinkoff.piapi.contract.v1.Currency;

/**
 * Maps {@link Currency} to {@link CurrencyInstrument}
 */
@Mapper(uses = {QuotationMapper.class, DateTimeMapper.class, MoneyMapper.class, CurrencyMapper.class})
public interface CurrencyInstrumentMapper {

    @Mapping(target = "lotSize", source = "lot")
    @Mapping(target = "country", source = "countryOfRiskName")
    @Mapping(target = "buyAvailable", source = "buyAvailableFlag")
    @Mapping(target = "sellAvailable", source = "sellAvailableFlag")
    @Mapping(target = "apiTradeAvailable", source = "apiTradeAvailableFlag")
    CurrencyInstrument map(final Currency source);

}