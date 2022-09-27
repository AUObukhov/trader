package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.Exchange;

/**
 * Maps {@link String} to {@link Exchange}
 */
@Mapper
public interface ExchangeMapper {

    default Exchange map(final String value) {
        return Exchange.fromValue(value);
    }

}