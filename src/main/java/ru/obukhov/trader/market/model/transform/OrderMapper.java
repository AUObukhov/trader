package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.trader.market.model.Order;
import ru.tinkoff.piapi.contract.v1.OrderState;

/**
 * Maps {@link ru.tinkoff.piapi.contract.v1.OrderState} to {@link ru.obukhov.trader.market.model.Order} and vice versa
 */
@Mapper(uses = {DateTimeMapper.class, MoneyValueMapper.class})
public interface OrderMapper {

    @Mapping(target = "quantityLots", source = "lotsExecuted")
    @Mapping(target = "commission", source = "executedCommission")
    @Mapping(target = "currency", source = "averagePositionPrice.currency")
    @Mapping(target = "type", source = "orderType")
    @Mapping(target = "dateTime", source = "orderDate")
    Order map(final OrderState orderState);

}
