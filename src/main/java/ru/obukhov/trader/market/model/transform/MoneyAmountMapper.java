package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.MoneyAmount;

/**
 * Maps {@link ru.tinkoff.invest.openapi.model.rest.MoneyAmount} to {@link MoneyAmount}
 */
@Mapper
public interface MoneyAmountMapper {

    MoneyAmount map(final ru.tinkoff.invest.openapi.model.rest.MoneyAmount source);

    ru.tinkoff.invest.openapi.model.rest.MoneyAmount map(final MoneyAmount source);

}