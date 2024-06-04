package ru.obukhov.trader.test.utils.model.order_state;

import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.test.utils.ResourceUtils;

public class TestOrderStates {

    public static final TestOrderState ORDER_STATE1 = readOrderState("order-state1.json");
    public static final TestOrderState ORDER_STATE2 = readOrderState("order-state2.json");

    private static TestOrderState readOrderState(final String fileName) {
        final OrderState orderState = ResourceUtils.getResourceAsObject("order_states/" + fileName, OrderState.class);
        return new TestOrderState(orderState);
    }

}