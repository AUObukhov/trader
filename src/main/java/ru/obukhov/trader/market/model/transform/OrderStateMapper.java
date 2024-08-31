package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.trader.market.model.OrderState;

@Mapper(uses = {OrderStageMapper.class, DateTimeMapper.class, MoneyValueMapper.class})
public interface OrderStateMapper {

    @Mapping(target = "stages", source = "stagesList")
    OrderState map(final ru.tinkoff.piapi.contract.v1.OrderState orderState);

}
