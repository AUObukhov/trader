package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.Bond;

/**
 * Maps {@link ru.tinkoff.piapi.contract.v1.Bond} to {@link Bond}
 */
@Mapper(uses = {QuotationMapper.class, DateTimeMapper.class, MoneyValueMapper.class})
public interface BondMapper {

    Bond map(final ru.tinkoff.piapi.contract.v1.Bond source);

}