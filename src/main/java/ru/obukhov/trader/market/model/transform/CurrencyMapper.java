package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.Currency;

/**
 * Maps {@link ru.tinkoff.piapi.contract.v1.Currency} to {@link Currency}
 */
@Mapper(uses = {QuotationMapper.class, DateTimeMapper.class, MoneyValueMapper.class})
public interface CurrencyMapper {

    Currency map(final ru.tinkoff.piapi.contract.v1.Currency source);

}