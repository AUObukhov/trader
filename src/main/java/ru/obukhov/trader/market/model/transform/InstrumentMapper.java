package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.Instrument;

/**
 * Maps {@link ru.tinkoff.piapi.contract.v1.Instrument} to {@link Instrument}
 */
@Mapper(uses = DateTimeMapper.class)
public interface InstrumentMapper {

    Instrument map(final ru.tinkoff.piapi.contract.v1.Instrument tinkoffInstrument);

}