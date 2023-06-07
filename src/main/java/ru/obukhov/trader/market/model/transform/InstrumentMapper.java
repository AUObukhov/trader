package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.trader.market.model.Instrument;

/**
 * Maps {@link ru.tinkoff.piapi.contract.v1.Instrument} to {@link Instrument}
 */
@Mapper(uses = {QuotationMapper.class, DateTimeMapper.class, MoneyMapper.class, CurrencyMapper.class})
public interface InstrumentMapper {

    @Mapping(target = "lotSize", source = "lot")
    @Mapping(target = "kLong", source = "klong")
    @Mapping(target = "kShort", source = "kshort")
    @Mapping(target = "dLong", source = "dlong")
    @Mapping(target = "dShort", source = "dshort")
    @Mapping(target = "dLongMin", source = "dlongMin")
    @Mapping(target = "dShortMin", source = "dshortMin")
    @Mapping(target = "buyAvailable", source = "buyAvailableFlag")
    @Mapping(target = "sellAvailable", source = "sellAvailableFlag")
    @Mapping(target = "apiTradeAvailable", source = "apiTradeAvailableFlag")
    Instrument map(final ru.tinkoff.piapi.contract.v1.Instrument source);

}