package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.test.utils.model.orderstate.TestOrderState1;

class OrderStateMapperUnitTest {

    private final OrderStateMapper orderStateMapper = Mappers.getMapper(OrderStateMapper.class);

    @Test
    void map() {
        final OrderState orderState = orderStateMapper.map(TestOrderState1.TINKOFF_ORDER_STATE);

        Assertions.assertEquals(TestOrderState1.ORDER_STATE, orderState);
    }

}