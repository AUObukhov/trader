package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.Instrument;

@Mapper(uses = {QuotationMapper.class, DateTimeMapper.class})
public interface InstrumentMapper {

    Instrument map(final ru.tinkoff.piapi.contract.v1.Instrument tInstrument);

}