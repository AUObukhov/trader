package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.trader.market.model.TradingSchedule;

/**
 * Maps {@link ru.tinkoff.piapi.contract.v1.TradingSchedule} to {@link TradingSchedule}
 */
@Mapper(uses = TradingDayMapper.class)
public interface TradingScheduleMapper {

    @Mapping(target = "days", source = "daysList")
    TradingSchedule map(final ru.tinkoff.piapi.contract.v1.TradingSchedule source);

}