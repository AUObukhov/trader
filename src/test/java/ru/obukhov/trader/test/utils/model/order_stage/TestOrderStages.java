package ru.obukhov.trader.test.utils.model.order_stage;

import ru.obukhov.trader.market.model.OrderStage;
import ru.obukhov.trader.test.utils.ResourceUtils;

public class TestOrderStages {

    public static final TestOrderStage ORDER_STAGE1 = readOrderStage("order-stage1.json");
    public static final TestOrderStage ORDER_STAGE2 = readOrderStage("order-stage2.json");

    private static TestOrderStage readOrderStage(final String fileName) {
        final OrderStage orderStage = ResourceUtils.getResourceAsObject("order_stages/" + fileName, OrderStage.class);
        return new TestOrderStage(orderStage);
    }

}