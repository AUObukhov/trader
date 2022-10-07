package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.TradingDay;

/**
 * Maps {@link ru.tinkoff.piapi.contract.v1.TradingDay} to {@link TradingDay}
 */
@Mapper(uses = DateTimeMapper.class)
public interface TradingDayMapper {

    TradingDay map(final ru.tinkoff.piapi.contract.v1.TradingDay source);

}