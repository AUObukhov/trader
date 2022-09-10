package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.Currency;

/**
 * Maps {@link String} to {@link Currency}
 */
@Mapper
public interface CurrencyMapper {

    default Currency map(final String value) {
        return Currency.valueOfIgnoreCase(value);
    }

}