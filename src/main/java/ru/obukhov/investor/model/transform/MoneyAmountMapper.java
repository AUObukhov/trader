package ru.obukhov.investor.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.investor.model.MoneyAmount;

/**
 * Maps {@link ru.tinkoff.invest.openapi.models.MoneyAmount} to {@link MoneyAmount}
 */
@Mapper
public interface MoneyAmountMapper {

    MoneyAmount map(ru.tinkoff.invest.openapi.models.MoneyAmount source);

    ru.tinkoff.invest.openapi.models.MoneyAmount map(MoneyAmount source);

}