package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.Dividend;

@Mapper(uses = {QuotationMapper.class, DateTimeMapper.class, MoneyValueMapper.class})
public interface DividendMapper {

    Dividend map(final ru.tinkoff.piapi.contract.v1.Dividend source);

}