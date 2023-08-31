package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import ru.obukhov.trader.market.model.TradingDay;

/**
 * Maps {@link ru.tinkoff.piapi.contract.v1.TradingDay} to {@link TradingDay}
 */
@Mapper(uses = DateTimeMapper.class, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface TradingDayMapper {

    TradingDay map(final ru.tinkoff.piapi.contract.v1.TradingDay source);

    ru.tinkoff.piapi.contract.v1.TradingDay map(final TradingDay source);

}