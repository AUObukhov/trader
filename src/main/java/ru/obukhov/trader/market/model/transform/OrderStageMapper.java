package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.OrderStage;

@Mapper(uses = MoneyValueMapper.class)
public interface OrderStageMapper {

    OrderStage map(final ru.tinkoff.piapi.contract.v1.OrderStage source);

    ru.tinkoff.piapi.contract.v1.OrderStage map(final OrderStage source);

}
