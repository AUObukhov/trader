package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.InstrumentType;

/**
 * Maps {@link String} to {@link InstrumentType}
 */
@Mapper
public interface InstrumentTypeMapper {

    default InstrumentType map(final String value) {
        return InstrumentType.fromValue(value);
    }

}