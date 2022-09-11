package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.Sector;

/**
 * Maps {@link String} to {@link Sector}
 */
@Mapper
public interface SectorMapper {

    default Sector map(final String value) {
        return Sector.valueOfIgnoreCase(value);
    }

}