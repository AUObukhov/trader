package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.OrderStage;

/**
 * Maps {@link ru.tinkoff.piapi.contract.v1.OrderStage} to {@link OrderStage} and vice versa
 */
@Mapper
public interface OrderStageMapper {

    OrderStage map(final ru.tinkoff.piapi.contract.v1.OrderStage source);

    ru.tinkoff.piapi.contract.v1.OrderStage map(final OrderStage source);

}
