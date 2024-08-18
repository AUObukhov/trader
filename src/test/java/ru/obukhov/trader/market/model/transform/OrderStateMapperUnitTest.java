package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.test.utils.model.order_state.TestOrderState;
import ru.obukhov.trader.test.utils.model.order_state.TestOrderStates;

class OrderStateMapperUnitTest {

    private final OrderStateMapper orderStateMapper = Mappers.getMapper(OrderStateMapper.class);

    @Test
    void map() {
        final TestOrderState testOrderState = TestOrderStates.ORDER_STATE1;

        final OrderState orderState = orderStateMapper.map(testOrderState.tOrderState());

        Assertions.assertEquals(testOrderState.orderState(), orderState);
    }

}