package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.Share;

/**
 * Maps {@link ru.tinkoff.piapi.contract.v1.Share} to {@link Share}
 */
@Mapper(uses = {QuotationMapper.class, DateTimeMapper.class, MoneyValueMapper.class})
public interface ShareMapper {

    Share map(final ru.tinkoff.piapi.contract.v1.Share source);

}