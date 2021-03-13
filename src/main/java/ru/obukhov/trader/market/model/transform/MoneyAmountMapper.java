package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.MoneyAmount;

/**
 * Maps {@link ru.tinkoff.invest.openapi.models.MoneyAmount} to {@link MoneyAmount}
 */
@Mapper
public interface MoneyAmountMapper {

    MoneyAmount map(ru.tinkoff.invest.openapi.models.MoneyAmount source);

    ru.tinkoff.invest.openapi.models.MoneyAmount map(MoneyAmount source);

}