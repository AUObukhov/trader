package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.trader.market.model.OrderState;

/**
 * Maps {@link ru.tinkoff.piapi.contract.v1.OrderState} to {@link ru.obukhov.trader.market.model.OrderState} and vice versa
 */
@Mapper(uses = DateTimeMapper.class)
public interface OrderStateMapper {

    @Mapping(target = "stages", source = "stagesList")
    OrderState map(final ru.tinkoff.piapi.contract.v1.OrderState orderState);

}
