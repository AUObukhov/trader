package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.Etf;

@Mapper(uses = {QuotationMapper.class, DateTimeMapper.class})
public interface EtfMapper {

    Etf map(final ru.tinkoff.piapi.contract.v1.Etf source);
}